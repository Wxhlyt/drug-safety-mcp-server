package com.drugsafety.controller;

import com.drugsafety.service.AgentDrugQueryService;
import com.drugsafety.service.AgentQueryException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Thin HTTP transport for the local, read-only MCP adapter. */
@RestController
@RequestMapping("/agent-query/v1")
@RequiredArgsConstructor
public class AgentDrugQueryController {

    private final AgentDrugQueryService service;

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        return service.searchDrug(keyword);
    }

    @GetMapping("/drugs/{drugId}/profile")
    public Map<String, Object> profile(@PathVariable long drugId) {
        return service.getDrugProfile(drugId);
    }

    @GetMapping("/drugs/{drugId}/evidence")
    public Map<String, Object> evidence(@PathVariable long drugId,
                                        @RequestParam String type,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return service.getSafetyEvidence(drugId, type, page, size);
    }

    @ExceptionHandler(AgentQueryException.class)
    public ResponseEntity<Map<String, String>> queryError(AgentQueryException error) {
        return ResponseEntity.status(error.getHttpStatus()).body(Map.of("error", error.getCode()));
    }
}
