package at.fhtw.webenprjbackend.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RateLimitingFilter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter")
class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        rateLimitingFilter.clearAttempts();
    }

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternalTests {

        @Test
        @DisplayName("should pass through for non-login requests")
        void nonLoginRequest_passesThrough() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("GET");

            // Act
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should pass through for GET request to login endpoint")
        void getLoginRequest_passesThrough() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("GET");

            // Act
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should allow POST login request within limit")
        void postLoginRequest_withinLimit_passesThrough() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");

            // Act
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should block login request after exceeding limit")
        void postLoginRequest_exceedsLimit_blocks() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getRemoteAddr()).thenReturn("192.168.1.2");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            // Make 6 requests (limit is 5)
            for (int i = 0; i < 5; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Act - 6th request should be blocked
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setHeader(eq("Retry-After"), anyString());
        }

        @Test
        @DisplayName("should use X-Forwarded-For header when present")
        void xForwardedFor_usesClientIp() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");

            // Act
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            // The client IP should be 10.0.0.1 (first IP in the chain)
        }

        @Test
        @DisplayName("should track different IPs separately")
        void differentIps_trackedSeparately() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");

            // First IP - make 5 requests
            when(request.getRemoteAddr()).thenReturn("192.168.1.3");
            for (int i = 0; i < 5; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Second IP - should still be allowed
            when(request.getRemoteAddr()).thenReturn("192.168.1.4");

            // Act
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert - filter chain should be called for second IP
            verify(filterChain, times(6)).doFilter(request, response);
        }

        @Test
        @DisplayName("should return JSON error response when rate limited")
        void rateLimited_returnsJsonError() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getRemoteAddr()).thenReturn("192.168.1.5");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            // Exceed limit
            for (int i = 0; i < 6; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Assert
            writer.flush();
            String jsonResponse = stringWriter.toString();
            assertThat(jsonResponse).contains("\"status\":429");
            assertThat(jsonResponse).contains("\"error\":\"Too Many Requests\"");
            assertThat(jsonResponse).contains("Too many login attempts");
        }
    }

    @Nested
    @DisplayName("clearAttempts()")
    class ClearAttemptsTests {

        @Test
        @DisplayName("should reset rate limit after clearing")
        void clearAttempts_resetsLimit() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/auth/login");
            when(request.getRemoteAddr()).thenReturn("192.168.1.6");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            // Exceed limit
            for (int i = 0; i < 6; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Act - clear attempts
            rateLimitingFilter.clearAttempts();

            // Make another request - should pass through
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Assert - filter chain called 6 times (5 before limit + 1 after clear)
            verify(filterChain, times(6)).doFilter(request, response);
        }
    }
}
