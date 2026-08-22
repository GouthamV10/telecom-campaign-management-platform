package com.telecom.campaign.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "rate-limit.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;
    private static final long BLOCK_MS = 300_000;

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RateBucket bucket = buckets.computeIfAbsent(clientIp, k -> new RateBucket());

        synchronized (bucket) {
            long now = System.currentTimeMillis();

            if (bucket.blockedUntil > now) {
                long remaining = (bucket.blockedUntil - now) / 1000;
                log.warn("Rate limit blocked for IP={}, remaining={}s", clientIp, remaining);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "success": false,
                            "statusCode": 429,
                            "message": "Too many login attempts. Please try again in %d seconds.",
                            "data": null
                        }""".formatted(remaining));
                return;
            }

            if (now - bucket.windowStart > WINDOW_MS) {
                bucket.windowStart = now;
                bucket.attempts.set(0);
            }

            bucket.attempts.incrementAndGet();

            if (bucket.attempts.get() > MAX_ATTEMPTS) {
                bucket.blockedUntil = now + BLOCK_MS;
                log.warn("Rate limit exceeded for IP={}, blocking for {}s", clientIp, BLOCK_MS / 1000);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "success": false,
                            "statusCode": 429,
                            "message": "Too many login attempts. Account temporarily blocked.",
                            "data": null
                        }""");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateBucket {
        long windowStart = System.currentTimeMillis();
        AtomicInteger attempts = new AtomicInteger(0);
        long blockedUntil = 0;
    }
}
