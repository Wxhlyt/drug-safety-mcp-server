package com.drugsafety.service;

import java.util.Map;

/** Read-only business boundary shared with the local MCP protocol adapter. */
public interface AgentDrugQueryService {
    Map<String, Object> searchDrug(String keyword);
    Map<String, Object> getDrugProfile(long drugId);
    Map<String, Object> getSafetyEvidence(long drugId, String evidenceType, int page, int pageSize);
}
