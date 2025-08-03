package com.beaver.userservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class GatewayFilter implements Filter {

    private final String gatewaySecret;

    public GatewayFilter(@Value("${gateway.secret}") String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
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

        if (!gatewaySecret.equals(httpRequest.getHeader("X-Gateway-Secret"))) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden: Invalid service secret\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
