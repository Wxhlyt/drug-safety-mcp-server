package com.drugsafety.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;

/** Dedicated loopback-only machine credential; never grants a user or admin role. */
public class AgentTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Drug-Safety-Agent-Token";
    private final Path tokenFile;

    public AgentTokenAuthenticationFilter(String tokenFile) {
        this.tokenFile = Path.of(tokenFile).toAbsolutePath().normalize();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/agent-query/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String supplied = request.getHeader(HEADER);
        if ("GET".equals(request.getMethod()) && supplied != null && supplied.length() <= 256
                && isLoopback(request.getRemoteAddr())) {
            String expected = readToken();
            if (expected.length() >= 32 && MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("local-mcp-agent", null,
                                List.of(new SimpleGrantedAuthority("AGENT_READ"))));
            }
        }
        chain.doFilter(request, response);
    }

    private String readToken() {
        try {
            return Files.isRegularFile(tokenFile)
                    ? Files.readString(tokenFile, StandardCharsets.UTF_8).trim() : "";
        } catch (IOException ignored) {
            return "";
        }
    }

    private boolean isLoopback(String address) {
        try {
            return InetAddress.getByName(address).isLoopbackAddress();
        } catch (Exception ignored) {
            return false;
        }
    }
}
