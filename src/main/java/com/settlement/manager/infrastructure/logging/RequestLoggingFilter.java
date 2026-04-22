package com.settlement.manager.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
        MDC.put("method",  req.getMethod());
        MDC.put("uri",     req.getRequestURI());
        MDC.put("userId",  extractUserId(req));
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }

    private String extractUserId(HttpServletRequest req) {
        return "anonymous";
    }
}