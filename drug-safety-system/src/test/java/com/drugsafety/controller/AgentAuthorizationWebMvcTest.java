package com.drugsafety.controller;

import com.drugsafety.config.JwtAuthenticationFilter;
import com.drugsafety.config.SecurityConfig;
import com.drugsafety.service.AgentDrugQueryService;
import com.drugsafety.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentDrugQueryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AgentAuthorizationWebMvcTest {

    private static final String TOKEN = randomToken();
    private static final Path TOKEN_FILE = makeTokenFile();

    @DynamicPropertySource
    static void tokenFile(DynamicPropertyRegistry registry) {
        registry.add("drug-safety.agent-token-file", TOKEN_FILE::toString);
    }

    @Autowired MockMvc mvc;
    @MockBean AgentDrugQueryService service;
    @MockBean UserService userService;

    @Test
    void requiresDedicatedToken() throws Exception {
        mvc.perform(get("/agent-query/v1/search").param("keyword", "BELINOSTAT"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/agent-query/v1/search").param("keyword", "BELINOSTAT")
                        .header("X-Drug-Safety-Agent-Token", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void administratorDoesNotGainAgentAccess() throws Exception {
        mvc.perform(get("/agent-query/v1/search").param("keyword", "BELINOSTAT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void localTokenCanOnlyReadAgentEndpoint() throws Exception {
        when(service.searchDrug("BELINOSTAT")).thenReturn(Map.of("count", 0));
        mvc.perform(get("/agent-query/v1/search").param("keyword", "BELINOSTAT")
                        .header("X-Drug-Safety-Agent-Token", TOKEN))
                .andExpect(status().isOk());
        mvc.perform(post("/agent-query/v1/search")
                        .header("X-Drug-Safety-Agent-Token", TOKEN))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/drug/list").header("X-Drug-Safety-Agent-Token", TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonLoopbackSourceCannotAuthenticate() throws Exception {
        mvc.perform(get("/agent-query/v1/search").param("keyword", "BELINOSTAT")
                        .header("X-Drug-Safety-Agent-Token", TOKEN)
                        .with(request -> { request.setRemoteAddr("192.0.2.1"); return request; }))
                .andExpect(status().isUnauthorized());
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static Path makeTokenFile() {
        try {
            Path path = Files.createTempFile("agent-test-token-", ".txt");
            Files.writeString(path, TOKEN);
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }
}
