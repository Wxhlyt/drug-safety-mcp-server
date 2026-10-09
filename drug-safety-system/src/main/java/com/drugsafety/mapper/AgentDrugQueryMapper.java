package com.drugsafety.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** SQL boundary for the read-only local agent API. No table name comes from a request. */
@Repository
@RequiredArgsConstructor
public class AgentDrugQueryMapper {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> search(String keyword, List<Long> allowedDrugIds) {
        String placeholders = String.join(",", allowedDrugIds.stream().map(id -> "?").toList());
        String sql = """
                SELECT c.drug_id, c.drug_name, c.external_compound_id, c.mapping_status,
                       c.external_batch_id, b.source_name, b.source_url, b.dataset_version,
                       b.archive_sha256,
                       (SELECT COUNT(*) FROM ext_frdb_ddi_staging x
                        JOIN ext_frdb_drug_staging d ON d.import_batch_id = x.import_batch_id
                         AND d.frdb_compound_id = x.frdb_compound_id
                        WHERE x.import_batch_id = 1 AND x.frdb_compound_id = c.external_compound_id) AS ddi_count,
                       (SELECT COUNT(*) FROM ext_frdb_adverse_event_staging a
                        JOIN ext_frdb_toxicity_staging t ON t.import_batch_id = a.import_batch_id
                         AND t.frdb_tox_id = a.frdb_tox_id
                        JOIN ext_frdb_drug_staging d ON d.import_batch_id = t.import_batch_id
                         AND d.frdb_compound_id = t.frdb_compound_id
                        WHERE a.import_batch_id = 1 AND d.frdb_compound_id = c.external_compound_id) AS adverse_count
                FROM v_ext_frdb_drug_catalog c
                JOIN ext_data_import_batch b ON b.id = c.external_batch_id
                WHERE c.drug_id IN (%s)
                  AND c.external_batch_id = 1 AND c.data_source = 'FRDB'
                  AND LOCATE(LOWER(?), LOWER(c.drug_name)) > 0
                ORDER BY c.drug_name, c.drug_id
                """.formatted(placeholders);
        List<Object> params = new ArrayList<>(allowedDrugIds);
        params.add(keyword);
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    public List<Map<String, Object>> catalog(long drugId, long expectedCompoundId) {
        return jdbcTemplate.queryForList("""
                SELECT c.drug_id, c.drug_name, c.generic_name, c.external_compound_id,
                       c.external_unii, c.external_batch_id, c.mapping_status, c.match_method,
                       c.match_confidence, c.candidate_count, c.review_status,
                       b.source_name, b.source_url, b.dataset_version, b.archive_sha256
                FROM v_ext_frdb_drug_catalog c
                JOIN ext_data_import_batch b ON b.id = c.external_batch_id
                WHERE c.drug_id = ? AND c.external_compound_id = ?
                  AND c.external_batch_id = 1 AND c.data_source = 'FRDB'
                """, drugId, expectedCompoundId);
    }

    public List<Map<String, Object>> matchedLabels(Object batchId, Object compoundId) {
        return jdbcTemplate.queryForList("""
                SELECT l.id, l.spl_set_id, l.spl_version, l.effective_time,
                       l.source_url, l.raw_file_sha256
                FROM ext_frdb_dailymed_mapping m
                JOIN ext_dailymed_spl_label l
                  ON l.import_batch_id = m.dailymed_import_batch_id
                 AND l.spl_set_id = m.spl_set_id AND l.spl_version = m.spl_version
                WHERE m.frdb_import_batch_id = ? AND m.frdb_compound_id = ?
                  AND m.mapping_status = 'MATCHED'
                LIMIT 2
                """, batchId, compoundId);
    }

    public List<Map<String, Object>> labelSections(Object labelId) {
        return jdbcTemplate.queryForList("""
                SELECT section_code, section_title
                FROM ext_dailymed_spl_section WHERE label_id = ? ORDER BY id
                """, labelId);
    }

    public long evidenceCount(long compoundId, String type) {
        String sql = switch (type) {
            case "ddi" -> """
                    SELECT COUNT(*) FROM ext_frdb_ddi_staging x
                    JOIN ext_frdb_drug_staging d ON d.import_batch_id = x.import_batch_id
                     AND d.frdb_compound_id = x.frdb_compound_id
                    WHERE x.import_batch_id = 1 AND x.frdb_compound_id = ?
                    """;
            case "adverse" -> """
                    SELECT COUNT(*) FROM ext_frdb_adverse_event_staging a
                    JOIN ext_frdb_toxicity_staging t ON t.import_batch_id = a.import_batch_id
                     AND t.frdb_tox_id = a.frdb_tox_id
                    JOIN ext_frdb_drug_staging d ON d.import_batch_id = t.import_batch_id
                     AND d.frdb_compound_id = t.frdb_compound_id
                    WHERE a.import_batch_id = 1 AND d.frdb_compound_id = ?
                    """;
            default -> throw new IllegalArgumentException("Unsupported evidence type");
        };
        Long total = jdbcTemplate.queryForObject(sql, Long.class, compoundId);
        return total == null ? 0 : total;
    }

    public List<Map<String, Object>> evidence(long compoundId, String type, int page, int size) {
        String sql = switch (type) {
            case "ddi" -> """
                    SELECT x.frdb_ddi_id, x.ddi_activity, x.ddi_clin_comment, x.ddi_clin_evidence,
                           x.ddi_clin_support, x.ddi_comment, x.ddi_concentration, x.ddi_metabolyte,
                           x.ddi_relation, x.ddi_reported_magnitude, x.ddi_magnitude, x.ddi_target,
                           x.ddi_type, x.ddi_url, x.ddi_page
                    FROM ext_frdb_ddi_staging x
                    JOIN ext_frdb_drug_staging d ON d.import_batch_id = x.import_batch_id
                     AND d.frdb_compound_id = x.frdb_compound_id
                    WHERE x.import_batch_id = 1 AND x.frdb_compound_id = ?
                    ORDER BY x.frdb_ddi_id LIMIT ? OFFSET ?
                    """;
            case "adverse" -> """
                    SELECT a.frdb_adverse_event_id, a.adverseevents_frequency, a.adverseevents_frequency_units,
                           a.adverseevents_liability, a.adverseevents_severity, a.adverseevents_type,
                           a.adverseevents_comment, a.adverseevents_dlt, t.frdb_tox_id,
                           t.toxicity_routes, t.toxicity_species, t.toxicity_source_uri
                    FROM ext_frdb_adverse_event_staging a
                    JOIN ext_frdb_toxicity_staging t ON t.import_batch_id = a.import_batch_id
                     AND t.frdb_tox_id = a.frdb_tox_id
                    JOIN ext_frdb_drug_staging d ON d.import_batch_id = t.import_batch_id
                     AND d.frdb_compound_id = t.frdb_compound_id
                    WHERE a.import_batch_id = 1 AND d.frdb_compound_id = ?
                    ORDER BY a.frdb_adverse_event_id LIMIT ? OFFSET ?
                    """;
            default -> throw new IllegalArgumentException("Unsupported evidence type");
        };
        return jdbcTemplate.queryForList(sql, compoundId, size, (page - 1) * size);
    }

}
