package com.drugsafety.service.impl;

import com.drugsafety.mapper.AgentDrugQueryMapper;
import com.drugsafety.service.AgentDrugQueryService;
import com.drugsafety.service.AgentQueryException;
import com.drugsafety.service.DemoDrugScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-side scope, provenance and mapping policy; no clinical interpretation. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentDrugQueryServiceImpl implements AgentDrugQueryService {

    private static final List<String> LIMITATIONS = List.of(
            "FRDB 相互作用与不良事件是外部原始证据，不等于患者个体诊断或用药建议。",
            "ddi_target 为原始文本，未核对其是否对应系统中的另一条药品记录。",
            "仅 MATCHED 表示当前导入流程获得唯一 DailyMed 标签匹配；其余状态不提供已确认标签。"
    );

    private final AgentDrugQueryMapper mapper;

    @Override
    public Map<String, Object> searchDrug(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() > 80) {
            throw new AgentQueryException("INVALID_KEYWORD", 400);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : mapper.search(keyword.trim(), DemoDrugScope.drugIds())) {
            long drugId = number(row.get("drug_id"));
            Long expectedCompound = DemoDrugScope.expectedCompound(drugId);
            if (expectedCompound == null || expectedCompound != number(row.get("external_compound_id"))) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("drug_id", row.get("drug_id"));
            item.put("drug_name", row.get("drug_name"));
            item.put("frdb_compound_id", row.get("external_compound_id"));
            item.put("mapping_status", row.get("mapping_status"));
            item.put("evidence_counts", Map.of(
                    "ddi", number(row.get("ddi_count")),
                    "adverse", number(row.get("adverse_count"))));
            item.put("source", sourceOf(row));
            items.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", keyword.trim());
        result.put("count", items.size());
        result.put("items", items);
        result.put("source", "NCATS Inxight Drugs FRDB");
        result.put("limitations", LIMITATIONS);
        return result;
    }

    @Override
    public Map<String, Object> getDrugProfile(long drugId) {
        Map<String, Object> row = catalogDrug(drugId);
        Map<String, Object> dailymed = null;
        if ("MATCHED".equals(row.get("mapping_status"))) {
            List<Map<String, Object>> labels = mapper.matchedLabels(
                    row.get("external_batch_id"), row.get("external_compound_id"));
            if (labels.size() == 1) {
                Map<String, Object> label = labels.getFirst();
                List<Map<String, Object>> sections = mapper.labelSections(label.get("id"));
                dailymed = new LinkedHashMap<>();
                dailymed.put("label_set_id", label.get("spl_set_id"));
                dailymed.put("label_version", label.get("spl_version"));
                dailymed.put("effective_time", label.get("effective_time"));
                dailymed.put("source_url", label.get("source_url"));
                dailymed.put("raw_file_sha256", label.get("raw_file_sha256"));
                dailymed.put("section_count", sections.size());
                dailymed.put("section_titles", sections);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("drug_id", row.get("drug_id"));
        result.put("drug_name", row.get("drug_name"));
        result.put("frdb_compound_id", row.get("external_compound_id"));
        result.put("unii", row.get("external_unii"));
        result.put("source", sourceOf(row));
        result.put("mapping_status", row.get("mapping_status"));
        result.put("mapping_method", row.get("match_method"));
        result.put("mapping_candidate_count", row.get("candidate_count"));
        result.put("dailymed", dailymed);
        result.put("limitations", LIMITATIONS);
        return result;
    }

    @Override
    public Map<String, Object> getSafetyEvidence(long drugId, String evidenceType, int page, int pageSize) {
        if (!"ddi".equals(evidenceType) && !"adverse".equals(evidenceType)) {
            throw new AgentQueryException("INVALID_EVIDENCE_TYPE", 400);
        }
        if (page < 1 || page > 10000 || pageSize < 1 || pageSize > 10) {
            throw new AgentQueryException("INVALID_PAGINATION", 400);
        }
        Map<String, Object> row = catalogDrug(drugId);
        long compoundId = number(row.get("external_compound_id"));
        long total = mapper.evidenceCount(compoundId, evidenceType);
        List<Map<String, Object>> items = mapper.evidence(compoundId, evidenceType, page, pageSize);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("drug_id", row.get("drug_id"));
        result.put("drug_name", row.get("drug_name"));
        result.put("frdb_compound_id", row.get("external_compound_id"));
        result.put("evidence_type", evidenceType);
        result.put("page", page);
        result.put("page_size", pageSize);
        result.put("total", total);
        result.put("returned_count", items.size());
        result.put("items", items);
        result.put("source", sourceOf(row));
        result.put("mapping_status", row.get("mapping_status"));
        result.put("limitations", LIMITATIONS);
        return result;
    }

    private Map<String, Object> catalogDrug(long drugId) {
        Long compoundId = DemoDrugScope.expectedCompound(drugId);
        if (compoundId == null) throw new AgentQueryException("DRUG_OUTSIDE_DEMO_SCOPE", 404);
        List<Map<String, Object>> rows = mapper.catalog(drugId, compoundId);
        if (rows.size() != 1) throw new AgentQueryException("DRUG_NOT_FOUND", 404);
        return rows.getFirst();
    }

    private Map<String, Object> sourceOf(Map<String, Object> row) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("name", row.get("source_name"));
        source.put("url", row.get("source_url"));
        source.put("version", row.get("dataset_version"));
        source.put("import_batch_id", row.get("external_batch_id"));
        source.put("archive_sha256", row.get("archive_sha256"));
        return source;
    }

    private long number(Object value) {
        return ((Number) value).longValue();
    }
}
