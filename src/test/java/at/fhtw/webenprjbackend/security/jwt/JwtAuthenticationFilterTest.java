package at.fhtw.webenprjbackend.security.jwt;

import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private static final String SECRET = "testSecretKeyForJWTTokens123456789012345678901234567890";

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtDecoder, userDetailsService);
        SecurityContextHolder.clearContext();
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private Claims createClaims(String subject) {
        // Create a real JWT token and parse it to get valid Claims
        String token = Jwts.builder()
                .subject(subject)
                .claim("uid", UUID.randomUUID().toString())
                .claim("role", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private UserPrincipal createPrincipal(String email, String username) {
        return new UserPrincipal(
                UUID.randomUUID(),
                email,
                username,
                "hashedPassword",
                "ROLE_USER",
                true
        );
    }

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternalTests {

        @Test
        @DisplayName("should set authentication when valid token provided")
        void validToken_setsAuthentication() throws Exception {
            // Arrange
            String token = "valid.jwt.token";
            Claims claims = createClaims("testuser");
            UserPrincipal principal = createPrincipal("test@example.com", "testuser");

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtDecoder.decode(token)).thenReturn(claims);
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .isEqualTo(principal);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should continue filter chain without authentication when no token")
        void noToken_continuesWithoutAuthentication() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verify(jwtDecoder, never()).decode(anyString());
        }

        @Test
        @DisplayName("should continue filter chain when Authorization header has no Bearer prefix")
        void noBearerPrefix_continuesWithoutAuthentication() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Basic credentials");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verify(jwtDecoder, never()).decode(anyString());
        }

        @Test
        @DisplayName("should clear context when token is invalid")
        void invalidToken_clearsContext() throws Exception {
            // Arrange
            String token = "invalid.jwt.token";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtDecoder.decode(token)).thenThrow(new JwtException("Invalid token"));

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should clear context when user not found")
        void userNotFound_clearsContext() throws Exception {
            // Arrange
            String token = "valid.jwt.token";
            Claims claims = createClaims("deleteduser");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtDecoder.decode(token)).thenReturn(claims);
            when(userDetailsService.loadUserByUsername("deleteduser"))
                    .thenThrow(new UsernameNotFoundException("User not found"));

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not override existing authentication")
        void existingAuthentication_doesNotOverride() throws Exception {
            // Arrange
            String token = "valid.jwt.token";
            UserPrincipal existingPrincipal = createPrincipal("existing@example.com", "existing");

            // Set existing authentication
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            existingPrincipal, null, existingPrincipal.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
            verify(jwtDecoder, never()).decode(anyString());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should handle empty Bearer token")
        void emptyBearerToken_continuesWithoutAuthentication() throws Exception {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer ");
            // Empty token will cause a JwtException from decoder (which filter catches)
            when(jwtDecoder.decode("")).thenThrow(new JwtException("Token cannot be empty"));

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should extract correct authorities from user details")
        void validToken_extractsAuthorities() throws Exception {
            // Arrange
            String token = "valid.jwt.token";
            Claims claims = createClaims("adminuser");
            UserPrincipal adminPrincipal = new UserPrincipal(
                    UUID.randomUUID(),
                    "admin@example.com",
                    "adminuser",
                    "hashedPassword",
                    "ROLE_ADMIN",
                    true
            );

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtDecoder.decode(token)).thenReturn(claims);
            when(userDetailsService.loadUserByUsername("adminuser")).thenReturn(adminPrincipal);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }
    }
}
