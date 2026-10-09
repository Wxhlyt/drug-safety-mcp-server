package com.drugsafety.controller;

import com.drugsafety.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only, traceable access to imported FRDB evidence and its DailyMed mapping state. */
@RestController
@RequestMapping("/external-drug")
@RequiredArgsConstructor
@Tag(name = "外部药品证据", description = "FRDB 原始证据和 DailyMed 映射状态；本接口不修改外部数据")
public class ExternalDrugEvidenceController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/{drugId}/overview")
    @Operation(summary = "查询外部药品来源、映射状态和版本")
    public Result<Map<String, Object>> overview(@PathVariable Long drugId) {
        List<Map<String, Object>> catalog = jdbcTemplate.queryForList("""
                SELECT c.*, b.source_name, b.source_url AS frdb_source_url, b.dataset_version,
                       b.downloaded_at, b.archive_sha256
                FROM v_ext_frdb_drug_catalog c
                JOIN ext_data_import_batch b ON b.id = c.external_batch_id
                WHERE c.drug_id = ?
                """, drugId);
        if (catalog.isEmpty()) return Result.error(404, "未找到该药品的 FRDB 外部来源记录");
        Map<String, Object> data = new LinkedHashMap<>(catalog.getFirst());
        List<Map<String, Object>> label = jdbcTemplate.queryForList("""
                SELECT l.id AS label_id, l.spl_set_id, l.spl_version, l.effective_time, l.product_type, l.labeler_name,
                       l.source_url, l.raw_file_sha256, l.imported_at
                FROM ext_frdb_dailymed_mapping m
                JOIN ext_dailymed_spl_label l ON l.import_batch_id=m.dailymed_import_batch_id
                    AND l.spl_set_id=m.spl_set_id AND l.spl_version=m.spl_version
                WHERE m.frdb_compound_id=? AND m.frdb_import_batch_id=?
                """, data.get("external_compound_id"), data.get("external_batch_id"));
        Map<String, Object> dailyMedLabel = label.isEmpty() ? null : label.getFirst();
        data.put("dailymedLabel", dailyMedLabel);
        if (dailyMedLabel != null) {
            long labelId = ((Number) dailyMedLabel.get("label_id")).longValue();
            data.put("dailymedProducts", jdbcTemplate.queryForList("""
                    SELECT product_identifier, product_name, active_ingredients, dosage_form, strength_text,
                           route_text, package_text, rxcui
                    FROM ext_dailymed_spl_product WHERE label_id=? ORDER BY product_identifier
                    """, labelId));
            data.put("dailymedSections", jdbcTemplate.queryForList("""
                    SELECT section_code, section_title, section_text, section_sha256
                    FROM ext_dailymed_spl_section WHERE label_id=?
                    ORDER BY id LIMIT 8
                    """, labelId));
        } else {
            data.put("dailymedProducts", List.of());
            data.put("dailymedSections", List.of());
        }
        return Result.success("查询成功", data);
    }

    @GetMapping("/{drugId}/ddi")
    @Operation(summary = "服务端分页查询 FRDB 药物相互作用原始证据")
    public Result<Map<String, Object>> ddi(@PathVariable Long drugId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return evidencePage(drugId, page, size, "v_ext_frdb_ddi_evidence", "frdb_ddi_id");
    }

    @GetMapping("/{drugId}/adverse")
    @Operation(summary = "服务端分页查询 FRDB 不良反应原始证据")
    public Result<Map<String, Object>> adverse(@PathVariable Long drugId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return evidencePage(drugId, page, size, "v_ext_frdb_adverse_evidence", "frdb_adverse_event_id");
    }

    private Result<Map<String, Object>> evidencePage(Long drugId, int page, int size, String view, String orderBy) {
        List<Map<String, Object>> source = jdbcTemplate.queryForList(
                "SELECT external_batch_id, external_compound_id FROM v_ext_frdb_drug_catalog WHERE drug_id=?", drugId);
        if (source.isEmpty()) return Result.error(404, "未找到该药品的 FRDB 外部来源记录");
        long compoundId = ((Number) source.getFirst().get("external_compound_id")).longValue();
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + view + " WHERE frdb_compound_id=?", Long.class, compoundId);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                "SELECT * FROM " + view + " WHERE frdb_compound_id=? ORDER BY " + orderBy + " LIMIT ? OFFSET ?",
                compoundId, safeSize, (safePage - 1) * safeSize);
        return Result.success("查询成功", Map.of("items", items, "total", total == null ? 0L : total, "page", safePage, "size", safeSize));
    }
}
