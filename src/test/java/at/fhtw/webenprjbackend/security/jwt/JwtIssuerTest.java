package at.fhtw.webenprjbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtIssuer}.
 */
@DisplayName("JwtIssuer")
class JwtIssuerTest {

    private static final String SECRET = "testSecretKeyForJWTTokens123456789012345678901234567890";
    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    private JwtIssuer jwtIssuer;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationMs(EXPIRATION_MS);

        jwtIssuer = new JwtIssuer(properties);
        jwtIssuer.initKey();

        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("issue()")
    class IssueTests {

        @Test
        @DisplayName("should issue valid JWT token with correct claims")
        void issue_validInput_returnsValidToken() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token = jwtIssuer.issue(userId, username, role);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("should include subject (username) in token")
        void issue_includesSubject() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);

            // Assert
            assertThat(claims.getSubject()).isEqualTo(username);
        }

        @Test
        @DisplayName("should include user ID claim")
        void issue_includesUserId() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);

            // Assert
            assertThat(claims.get("uid", String.class)).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("should include role claim")
        void issue_includesRole() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_ADMIN";

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);

            // Assert
            assertThat(claims.get("role", String.class)).isEqualTo(role);
        }

        @Test
        @DisplayName("should include issued-at timestamp")
        void issue_includesIssuedAt() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);

            // Assert
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getIssuedAt().getTime())
                    .isLessThanOrEqualTo(System.currentTimeMillis());
        }

        @Test
        @DisplayName("should include expiration timestamp in the future")
        void issue_includesExpiration() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);

            // Assert
            assertThat(claims.getExpiration()).isNotNull();
            assertThat(claims.getExpiration().getTime())
                    .isGreaterThan(System.currentTimeMillis());
        }

        @Test
        @DisplayName("should set expiration based on configured expiration time")
        void issue_expirationMatchesConfig() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";
            long beforeIssue = System.currentTimeMillis();

            // Act
            String token = jwtIssuer.issue(userId, username, role);
            Claims claims = parseClaims(token);
            long afterIssue = System.currentTimeMillis();

            // Assert
            long expirationTime = claims.getExpiration().getTime();
            // Expiration should be within a reasonable range
            assertThat(expirationTime)
                    .isGreaterThanOrEqualTo(beforeIssue + EXPIRATION_MS - 1000)
                    .isLessThanOrEqualTo(afterIssue + EXPIRATION_MS + 1000);
        }

        @Test
        @DisplayName("should generate unique tokens for different users")
        void issue_generatesUniqueTokensForDifferentUsers() {
            // Arrange
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";

            // Act
            String token1 = jwtIssuer.issue(userId1, username, role);
            String token2 = jwtIssuer.issue(userId2, username, role);

            // Assert - tokens should be different due to different user IDs
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
