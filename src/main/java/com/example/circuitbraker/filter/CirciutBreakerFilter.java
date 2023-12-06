package com.example.circuitbraker.filter;


import com.example.circuitbraker.service.CircuitBreakerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CirciutBreakerFilter extends OncePerRequestFilter {
    int maxErrorCount = 5;
    @Autowired
    public CirciutBreakerFilter(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }
    private final CircuitBreakerService circuitBreakerService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        int count = circuitBreakerService.getMethodCount(request.getServletPath());
        System.out.println("count: " + count);
        if (count == maxErrorCount) {
            if (circuitBreakerService.isCircuitOpen(request.getServletPath())) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("Service is unavailable");
                return;
            }
        }

        doFilter(request, response, filterChain);
    }
}
