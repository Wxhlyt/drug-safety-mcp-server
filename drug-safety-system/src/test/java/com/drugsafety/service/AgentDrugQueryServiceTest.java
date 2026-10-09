package com.drugsafety.service;

import com.drugsafety.mapper.AgentDrugQueryMapper;
import com.drugsafety.service.impl.AgentDrugQueryServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AgentDrugQueryServiceTest {

    private final AgentDrugQueryMapper mapper = mock(AgentDrugQueryMapper.class);
    private final AgentDrugQueryService service = new AgentDrugQueryServiceImpl(mapper);

    @Test
    void demoScopeHasThirtyUniqueMappings() {
        assertEquals(30, DemoDrugScope.drugIds().size());
        assertEquals(21L, DemoDrugScope.expectedCompound(26L));
    }

    @Test
    void rejectsOutsideDemoScopeBeforeDatabaseAccess() {
        AgentQueryException error = assertThrows(AgentQueryException.class,
                () -> service.getDrugProfile(1));
        assertEquals("DRUG_OUTSIDE_DEMO_SCOPE", error.getCode());
        verifyNoInteractions(mapper);
    }

    @Test
    void searchDropsUnexpectedCompoundEvenForAllowedDrugId() {
        when(mapper.search(org.mockito.ArgumentMatchers.eq("BELINOSTAT"), anyList()))
                .thenReturn(List.of(Map.of("drug_id", 26L, "external_compound_id", 999L)));
        Map<String, Object> result = service.searchDrug("BELINOSTAT");
        assertEquals(0, result.get("count"));
    }

    @Test
    void ambiguousMappingNeverLooksUpDailyMedLabel() {
        when(mapper.catalog(1550L, 2268L)).thenReturn(List.of(Map.of(
                "drug_id", 1550L, "drug_name", "TEST", "external_compound_id", 2268L,
                "external_batch_id", 1L, "mapping_status", "AMBIGUOUS")));
        Map<String, Object> profile = service.getDrugProfile(1550L);
        assertEquals("AMBIGUOUS", profile.get("mapping_status"));
        assertNull(profile.get("dailymed"));
    }

    @Test
    void reviewAndUnmatchedStatusesRemainDistinctWithoutConfirmedLabel() {
        for (String status : List.of("NEEDS_REVIEW", "UNMATCHED")) {
            when(mapper.catalog(3030L, 10052L)).thenReturn(List.of(Map.of(
                    "drug_id", 3030L, "drug_name", "TEST", "external_compound_id", 10052L,
                    "external_batch_id", 1L, "mapping_status", status)));
            Map<String, Object> profile = service.getDrugProfile(3030L);
            assertEquals(status, profile.get("mapping_status"));
            assertNull(profile.get("dailymed"));
        }
    }

    @Test
    void matchedStatusStillRequiresExactlyOneLabel() {
        when(mapper.catalog(26L, 21L)).thenReturn(List.of(Map.of(
                "drug_id", 26L, "drug_name", "BELINOSTAT", "external_compound_id", 21L,
                "external_batch_id", 1L, "mapping_status", "MATCHED")));
        when(mapper.matchedLabels(1L, 21L)).thenReturn(List.of(Map.of("id", 1L), Map.of("id", 2L)));
        Map<String, Object> profile = service.getDrugProfile(26L);
        assertEquals("MATCHED", profile.get("mapping_status"));
        assertNull(profile.get("dailymed"));
    }

    @Test
    void rejectsEvidencePageAboveTen() {
        AgentQueryException error = assertThrows(AgentQueryException.class,
                () -> service.getSafetyEvidence(26L, "ddi", 1, 11));
        assertEquals("INVALID_PAGINATION", error.getCode());
        verifyNoInteractions(mapper);
    }
}
