package com.drugsafety.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Deterministic summary of imported source rows; it does not assign a clinical risk grade. */
@Service
@RequiredArgsConstructor
public class SourceEvidenceAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> analyze(Long drugId) {
        List<Map<String, Object>> catalog = jdbcTemplate.queryForList("""
                SELECT c.drug_id, c.drug_name, c.external_compound_id, c.mapping_status,
                       c.candidate_count, c.spl_set_id, c.spl_version,
                       b.source_name, b.source_url, b.dataset_version,
                       l.source_url AS label_source_url
                FROM v_ext_frdb_drug_catalog c
                JOIN ext_data_import_batch b ON b.id = c.external_batch_id
                LEFT JOIN ext_frdb_dailymed_mapping m
                  ON m.frdb_import_batch_id = c.external_batch_id
                 AND m.frdb_compound_id = c.external_compound_id
                LEFT JOIN ext_dailymed_spl_label l
                  ON l.import_batch_id = m.dailymed_import_batch_id
                 AND l.spl_set_id = m.spl_set_id AND l.spl_version = m.spl_version
                WHERE c.drug_id = ?
                """, drugId);
        if (catalog.isEmpty()) return null;

        Map<String, Object> drug = catalog.getFirst();
        long compoundId = ((Number) drug.get("external_compound_id")).longValue();
        Map<String, Object> ddi = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS total,
                       COUNT(NULLIF(TRIM(ddi_url), '')) AS with_source
                FROM v_ext_frdb_ddi_evidence WHERE frdb_compound_id = ?
                """, compoundId);
        Map<String, Object> adverse = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS total,
                       COUNT(NULLIF(TRIM(toxicity_source_uri), '')) AS with_source
                FROM v_ext_frdb_adverse_evidence WHERE frdb_compound_id = ?
                """, compoundId);
        long ddiCount = count(ddi, "total");
        long adverseCount = count(adverse, "total");
        String mappingStatus = drug.get("mapping_status") == null ? "UNKNOWN" : drug.get("mapping_status").toString();
        long candidates = drug.get("candidate_count") instanceof Number value ? value.longValue() : 0;
        boolean uniqueLabel = "MATCHED".equals(mappingStatus) && candidates == 1
                && hasText(drug.get("spl_set_id")) && hasText(drug.get("spl_version"));
        String labelStatus = uniqueLabel ? "CONFIRMED"
                : "UNMATCHED".equals(mappingStatus) ? "NO_CONFIRMED_LABEL" : "PENDING_REVIEW";
        String ddiStatus = ddiCount > 0 ? "HAS_RECORDS" : "NO_RECORDS";
        String adverseStatus = adverseCount > 0 ? "HAS_RECORDS" : "NO_RECORDS";
        String displayPolicy = uniqueLabel ? "CONFIRMED_LABEL_AND_EVIDENCE"
                : ddiCount > 0 || adverseCount > 0 ? "RAW_EVIDENCE_ONLY" : "SOURCE_METADATA_ONLY";

        List<Map<String, Object>> ddiSamples = jdbcTemplate.queryForList("""
                SELECT frdb_ddi_id, ddi_target, ddi_relation, ddi_url
                FROM v_ext_frdb_ddi_evidence WHERE frdb_compound_id = ?
                ORDER BY frdb_ddi_id LIMIT 5
                """, compoundId);
        List<Map<String, Object>> adverseSamples = jdbcTemplate.queryForList("""
                SELECT frdb_adverse_event_id, adverseevents_type, adverseevents_severity, toxicity_source_uri
                FROM v_ext_frdb_adverse_evidence WHERE frdb_compound_id = ?
                ORDER BY frdb_adverse_event_id LIMIT 5
                """, compoundId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("drugId", drugId);
        result.put("drugName", drug.get("drug_name"));
        result.put("frdbCompoundId", compoundId);
        result.put("sourceName", drug.get("source_name"));
        result.put("sourceUrl", drug.get("source_url"));
        result.put("datasetVersion", drug.get("dataset_version"));
        result.put("ddiCount", ddiCount);
        result.put("ddiWithSourceCount", count(ddi, "with_source"));
        result.put("ddiHasSourceLink", count(ddi, "with_source") > 0);
        result.put("ddiStatus", ddiStatus);
        result.put("adverseCount", adverseCount);
        result.put("adverseWithSourceCount", count(adverse, "with_source"));
        result.put("adverseHasSourceLink", count(adverse, "with_source") > 0);
        result.put("adverseStatus", adverseStatus);
        result.put("mappingStatus", mappingStatus);
        result.put("candidateCount", candidates);
        result.put("confirmedLabel", uniqueLabel);
        result.put("confirmedLabelVersion", uniqueLabel ? drug.get("spl_version") : null);
        result.put("confirmedLabelUrl", uniqueLabel ? drug.get("label_source_url") : null);
        result.put("labelStatus", labelStatus);
        result.put("displayPolicy", displayPolicy);
        result.put("ddiSamples", ddiSamples);
        result.put("adverseSamples", adverseSamples);
        result.put("evidenceStatus", ddiCount + adverseCount > 0 ? "HAS_SOURCE_ROWS" : "NO_SOURCE_ROWS");
        result.put("analysisText", String.format(
                "按固定规则核对来源库：相互作用原始记录 %d 条（其中 %d 条有非空来源字段），不良事件原始记录 %d 条（其中 %d 条有非空来源字段）。%s",
                ddiCount, count(ddi, "with_source"), adverseCount, count(adverse, "with_source"),
                uniqueLabel ? "存在唯一已确认的 DailyMed 标签映射。" : "DailyMed 标签映射未达到唯一确认条件。"));
        result.put("limitation", "仅用于资料检索、来源核对和系统演示；不构成临床风险评级、诊断、处方审核、剂量建议或联用禁忌结论。");
        return result;
    }

    private static boolean hasText(Object value) {
        return value != null && !value.toString().isBlank();
    }

    private static long count(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof Number number ? number.longValue() : 0;
    }
}
