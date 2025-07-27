package com.beaver.userservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class GatewayFilter implements Filter {

    private final String expectedSecret;

    public GatewayFilter(@Value("${service.secret}") String expectedSecret) {
        this.expectedSecret = expectedSecret;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (path.startsWith("/actuator") || path.equals("/health")) {
            chain.doFilter(request, response);
            return;
        }

        // Debug logging - remove after fixing
        String receivedSecret = httpRequest.getHeader("X-Service-Secret");
        String sourceHeader = httpRequest.getHeader("X-Source");

        // Validate X-Service-Secret header (shared secret)
        if (!expectedSecret.equals(receivedSecret)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden: Invalid service secret\"}");
            return;
        }

        // Validate X-Source header (must come from gateway)
        if (!"gateway".equals(sourceHeader)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden: Invalid request source\"}");
            return;
        }

        // All validations passed, continue with the request
        chain.doFilter(request, response);
    }
}
