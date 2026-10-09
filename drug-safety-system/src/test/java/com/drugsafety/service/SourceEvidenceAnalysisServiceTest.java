package com.drugsafety.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceEvidenceAnalysisServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private SourceEvidenceAnalysisService service;

    @Test
    void summarizesOnlyTraceableSourceFactsWithoutClinicalScore() {
        when(jdbcTemplate.queryForList(contains("FROM v_ext_frdb_drug_catalog"), org.mockito.ArgumentMatchers.eq(26L)))
                .thenReturn(List.of(Map.of(
                        "drug_name", "BELINOSTAT", "external_compound_id", 21L,
                        "mapping_status", "MATCHED", "candidate_count", 1L,
                        "spl_set_id", "test-set", "spl_version", "1",
                        "label_source_url", "https://example.org/confirmed.xml",
                        "source_name", "NCATS FRDB", "source_url", "https://example.org/frdb",
                        "dataset_version", "v1.5")));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_ddi_evidence"), org.mockito.ArgumentMatchers.eq(21L)))
                .thenReturn(Map.of("total", 14L, "with_source", 12L));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_adverse_evidence"), org.mockito.ArgumentMatchers.eq(21L)))
                .thenReturn(Map.of("total", 58L, "with_source", 55L));
        when(jdbcTemplate.queryForList(contains("SELECT frdb_ddi_id"), org.mockito.ArgumentMatchers.eq(21L)))
                .thenReturn(List.of(Map.of("frdb_ddi_id", 485L)));
        when(jdbcTemplate.queryForList(contains("SELECT frdb_adverse_event_id"), org.mockito.ArgumentMatchers.eq(21L)))
                .thenReturn(List.of(Map.of("frdb_adverse_event_id", 1L)));

        Map<String, Object> result = service.analyze(26L);

        assertEquals(14L, result.get("ddiCount"));
        assertEquals(58L, result.get("adverseCount"));
        assertEquals(12L, result.get("ddiWithSourceCount"));
        assertTrue((Boolean) result.get("confirmedLabel"));
        assertEquals("https://example.org/confirmed.xml", result.get("confirmedLabelUrl"));
        assertEquals("CONFIRMED", result.get("labelStatus"));
        assertEquals("HAS_RECORDS", result.get("ddiStatus"));
        assertEquals("HAS_RECORDS", result.get("adverseStatus"));
        assertEquals("CONFIRMED_LABEL_AND_EVIDENCE", result.get("displayPolicy"));
        assertTrue((Boolean) result.get("ddiHasSourceLink"));
        assertTrue((Boolean) result.get("adverseHasSourceLink"));
        assertFalse(result.containsKey("riskScore"));
        assertFalse(result.containsKey("riskLevel"));
    }

    @Test
    void missingFrdbMappingProducesNoAnalysis() {
        when(jdbcTemplate.queryForList(contains("FROM v_ext_frdb_drug_catalog"), org.mockito.ArgumentMatchers.eq(999L)))
                .thenReturn(List.of());

        assertNull(service.analyze(999L));
    }

    @Test
    void ambiguousLabelMappingIsNotConfirmed() {
        when(jdbcTemplate.queryForList(contains("FROM v_ext_frdb_drug_catalog"), org.mockito.ArgumentMatchers.eq(1550L)))
                .thenReturn(List.of(Map.of(
                        "drug_name", "WARFARIN", "external_compound_id", 2268L,
                        "mapping_status", "AMBIGUOUS", "candidate_count", 117L,
                        "source_name", "NCATS FRDB", "source_url", "https://example.org/frdb",
                        "dataset_version", "v1.5")));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_ddi_evidence"), org.mockito.ArgumentMatchers.eq(2268L)))
                .thenReturn(Map.of("total", 8L, "with_source", 8L));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_adverse_evidence"), org.mockito.ArgumentMatchers.eq(2268L)))
                .thenReturn(Map.of("total", 10L, "with_source", 10L));
        when(jdbcTemplate.queryForList(contains("SELECT frdb_ddi_id"), org.mockito.ArgumentMatchers.eq(2268L)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForList(contains("SELECT frdb_adverse_event_id"), org.mockito.ArgumentMatchers.eq(2268L)))
                .thenReturn(List.of());

        Map<String, Object> result = service.analyze(1550L);

        assertFalse((Boolean) result.get("confirmedLabel"));
        assertNull(result.get("confirmedLabelUrl"));
        assertEquals("PENDING_REVIEW", result.get("labelStatus"));
        assertEquals("RAW_EVIDENCE_ONLY", result.get("displayPolicy"));
        assertTrue(result.get("analysisText").toString().contains("未达到唯一确认条件"));
    }

    @Test
    void unmatchedLabelAndNoRowsShowOnlySourceMetadata() {
        when(jdbcTemplate.queryForList(contains("FROM v_ext_frdb_drug_catalog"), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(List.of(Map.of(
                        "drug_name", "SOURCE DRUG", "external_compound_id", 7L,
                        "mapping_status", "UNMATCHED", "candidate_count", 0L,
                        "source_name", "NCATS FRDB", "source_url", "https://example.org/frdb",
                        "dataset_version", "v1.5")));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_ddi_evidence"), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(Map.of("total", 0L, "with_source", 0L));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_adverse_evidence"), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(Map.of("total", 0L, "with_source", 0L));
        when(jdbcTemplate.queryForList(contains("SELECT frdb_ddi_id"), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForList(contains("SELECT frdb_adverse_event_id"), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(List.of());

        Map<String, Object> result = service.analyze(7L);

        assertEquals("NO_CONFIRMED_LABEL", result.get("labelStatus"));
        assertEquals("NO_RECORDS", result.get("ddiStatus"));
        assertEquals("NO_RECORDS", result.get("adverseStatus"));
        assertEquals("SOURCE_METADATA_ONLY", result.get("displayPolicy"));
        assertFalse((Boolean) result.get("ddiHasSourceLink"));
        assertFalse((Boolean) result.get("adverseHasSourceLink"));
    }

    @Test
    void matchedCodeWithoutCompleteLabelIdentityStillNeedsReview() {
        when(jdbcTemplate.queryForList(contains("FROM v_ext_frdb_drug_catalog"), org.mockito.ArgumentMatchers.eq(8L)))
                .thenReturn(List.of(Map.of(
                        "drug_name", "SOURCE DRUG", "external_compound_id", 8L,
                        "mapping_status", "MATCHED", "candidate_count", 1L,
                        "spl_set_id", " ", "spl_version", "1",
                        "source_name", "NCATS FRDB", "source_url", "https://example.org/frdb",
                        "dataset_version", "v1.5")));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_ddi_evidence"), org.mockito.ArgumentMatchers.eq(8L)))
                .thenReturn(Map.of("total", 1L, "with_source", 0L));
        when(jdbcTemplate.queryForMap(contains("FROM v_ext_frdb_adverse_evidence"), org.mockito.ArgumentMatchers.eq(8L)))
                .thenReturn(Map.of("total", 0L, "with_source", 0L));
        when(jdbcTemplate.queryForList(contains("SELECT frdb_ddi_id"), org.mockito.ArgumentMatchers.eq(8L)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForList(contains("SELECT frdb_adverse_event_id"), org.mockito.ArgumentMatchers.eq(8L)))
                .thenReturn(List.of());

        Map<String, Object> result = service.analyze(8L);

        assertEquals("PENDING_REVIEW", result.get("labelStatus"));
        assertEquals("RAW_EVIDENCE_ONLY", result.get("displayPolicy"));
        assertFalse((Boolean) result.get("ddiHasSourceLink"));
    }
}
