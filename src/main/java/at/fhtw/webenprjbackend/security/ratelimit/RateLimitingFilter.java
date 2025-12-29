package at.fhtw.webenprjbackend.security.ratelimit;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting filter that restricts login attempts per IP address.
 * Uses an in-memory map to track attempts within a time window.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final String LOGIN_PATH = "/auth/login";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final ConcurrentHashMap<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Only rate limit POST to /auth/login
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RateLimitEntry entry = attempts.compute(clientIp, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RateLimitEntry();
            }
            existing.increment();
            return existing;
        });

        if (entry.getAttempts() > MAX_ATTEMPTS) {
            throw new RateLimitException(entry.getSecondsUntilReset());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());
    }

    private String getClientIp(HttpServletRequest request) {
        // Check X-Forwarded-For header first (for proxies/load balancers)
        String forwarded = request.getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2
            // The first one is the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
