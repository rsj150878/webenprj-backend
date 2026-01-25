package at.fhtw.webenprjbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtDecoder}.
 */
@DisplayName("JwtDecoder")
class JwtDecoderTest {

    private static final String SECRET = "testSecretKeyForJWTTokens123456789012345678901234567890";
    private static final String DIFFERENT_SECRET = "differentSecretKeyForJWTTokens12345678901234567890";
    private static final long EXPIRATION_MS = 86400000L;

    private JwtDecoder jwtDecoder;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationMs(EXPIRATION_MS);

        jwtDecoder = new JwtDecoder(properties);
        jwtDecoder.initKey();

        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createValidToken(String username, UUID userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(username)
                .claim("uid", userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private String createExpiredToken(String username, UUID userId) {
        Date past = new Date(System.currentTimeMillis() - 1000);
        Date expiry = new Date(System.currentTimeMillis() - 500);

        return Jwts.builder()
                .subject(username)
                .claim("uid", userId.toString())
                .issuedAt(past)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Nested
    @DisplayName("decode()")
    class DecodeTests {

        @Test
        @DisplayName("should decode valid token successfully")
        void decode_validToken_returnsClaims() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_USER";
            String token = createValidToken(username, userId, role);

            // Act
            Claims claims = jwtDecoder.decode(token);

            // Assert
            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.get("uid", String.class)).isEqualTo(userId.toString());
            assertThat(claims.get("role", String.class)).isEqualTo(role);
        }

        @Test
        @DisplayName("should throw JwtException for expired token")
        void decode_expiredToken_throwsException() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String token = createExpiredToken("testuser", userId);

            // Act & Assert
            assertThatThrownBy(() -> jwtDecoder.decode(token))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("should throw JwtException for malformed token")
        void decode_malformedToken_throwsException() {
            // Arrange
            String malformedToken = "not.a.valid.jwt.token";

            // Act & Assert
            assertThatThrownBy(() -> jwtDecoder.decode(malformedToken))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("should throw JwtException for token signed with different key")
        void decode_wrongKey_throwsException() {
            // Arrange
            SecretKey wrongKey = Keys.hmacShaKeyFor(DIFFERENT_SECRET.getBytes(StandardCharsets.UTF_8));
            String tokenWithWrongKey = Jwts.builder()
                    .subject("testuser")
                    .claim("uid", UUID.randomUUID().toString())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(wrongKey)
                    .compact();

            // Act & Assert
            assertThatThrownBy(() -> jwtDecoder.decode(tokenWithWrongKey))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("should throw exception for empty token")
        void decode_emptyToken_throwsException() {
            // Act & Assert
            assertThatThrownBy(() -> jwtDecoder.decode(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw JwtException for null token")
        void decode_nullToken_throwsException() {
            // Act & Assert
            assertThatThrownBy(() -> jwtDecoder.decode(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should correctly extract all standard claims")
        void decode_extractsAllStandardClaims() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String role = "ROLE_ADMIN";
            String token = createValidToken(username, userId, role);

            // Act
            Claims claims = jwtDecoder.decode(token);

            // Assert
            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
            assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        }
    }
}
