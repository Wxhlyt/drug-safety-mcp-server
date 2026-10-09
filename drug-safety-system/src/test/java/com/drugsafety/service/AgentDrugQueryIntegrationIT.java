package com.drugsafety.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opt-in read-only integration test against the configured local demo database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AgentDrugQueryIntegrationIT {

    @Autowired AgentDrugQueryService service;

    @Test
    void readsRealFrdbEvidenceThroughSpringServiceAndMapper() {
        Map<String, Object> search = service.searchDrug("BELINOSTAT");
        assertEquals(1, search.get("count"));
        Map<?, ?> item = (Map<?, ?>) ((java.util.List<?>) search.get("items")).getFirst();
        assertEquals(26L, ((Number) item.get("drug_id")).longValue());

        Map<String, Object> profile = service.getDrugProfile(26L);
        assertEquals("MATCHED", profile.get("mapping_status"));
        assertNotNull(profile.get("dailymed"));

        Map<String, Object> ambiguous = service.getDrugProfile(1550L);
        assertEquals("AMBIGUOUS", ambiguous.get("mapping_status"));
        assertNull(ambiguous.get("dailymed"));

        Map<String, Object> evidence = service.getSafetyEvidence(26L, "ddi", 1, 2);
        assertEquals(2, evidence.get("returned_count"));
        assertTrue(((Number) evidence.get("total")).longValue() >= 2);
    }
}
