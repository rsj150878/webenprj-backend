package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.*;
import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security configuration covering JWT validation and authorization.
 * Tests protected endpoints, JWT expiration, and role-based access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostBookmarkRepository postBookmarkRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenIssuer tokenIssuer;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private User regularUser;
    private User adminUser;
    private String regularUserToken;
    private String adminUserToken;

    @BeforeEach
    void setUp() {
        // Clean up in order of foreign key dependencies
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        regularUser = new User(
                "regular@example.com",
                "regularuser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        regularUser = userRepository.save(regularUser);

        adminUser = new User(
                "admin@example.com",
                "adminuser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.ADMIN
        );
        adminUser = userRepository.save(adminUser);

        regularUserToken = tokenIssuer.issue(regularUser.getId(), regularUser.getUsername(), "USER");
        adminUserToken = tokenIssuer.issue(adminUser.getId(), adminUser.getUsername(), "ADMIN");
    }

    @Nested
    @DisplayName("Protected Endpoints - Authentication Required")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("should return 403 when accessing protected endpoint without token")
        void protectedEndpointWithoutToken_returns403() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 when accessing protected endpoint with invalid token")
        void protectedEndpointWithInvalidToken_returns403() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 when accessing protected endpoint with malformed Authorization header")
        void protectedEndpointWithMalformedHeader_returns403() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "NotBearer " + regularUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 when accessing protected endpoint with valid token")
        void protectedEndpointWithValidToken_returns200() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(regularUser.getEmail()))
                    .andExpect(jsonPath("$.username").value(regularUser.getUsername()));
        }
    }

    @Nested
    @DisplayName("JWT Expiration Tests")
    class JwtExpirationTests {

        @Test
        @DisplayName("should return 403 when accessing protected endpoint with expired token")
        void protectedEndpointWithExpiredToken_returns403() throws Exception {
            // Create an expired token (expired 1 hour ago)
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiredTime = new Date(now.getTime() - 3600000); // 1 hour ago

            String expiredToken = Jwts.builder()
                    .subject(regularUser.getUsername())
                    .claim("uid", regularUser.getId().toString())
                    .claim("role", "USER")
                    .issuedAt(new Date(now.getTime() - 7200000)) // issued 2 hours ago
                    .expiration(expiredTime)
                    .signWith(key)
                    .compact();

            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 when token is signed with wrong secret")
        void protectedEndpointWithWrongSecretToken_returns403() throws Exception {
            SecretKey wrongKey = Keys.hmacShaKeyFor("wrongSecretKeyForTestingPurposes12345678901234567890".getBytes(StandardCharsets.UTF_8));
            Date now = new Date();

            String wrongSecretToken = Jwts.builder()
                    .subject(regularUser.getUsername())
                    .claim("uid", regularUser.getId().toString())
                    .claim("role", "USER")
                    .issuedAt(now)
                    .expiration(new Date(now.getTime() + 3600000))
                    .signWith(wrongKey)
                    .compact();

            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + wrongSecretToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Admin Endpoints - Role-Based Access Control")
    class AdminEndpointTests {

        @Test
        @DisplayName("should return 403 when regular user accesses admin endpoint")
        void adminEndpointWithRegularUser_returns403() throws Exception {
            mockMvc.perform(get("/users")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 when admin user accesses admin endpoint")
        void adminEndpointWithAdminUser_returns200() throws Exception {
            mockMvc.perform(get("/users")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 403 when regular user tries to get user by ID")
        void getUserByIdWithRegularUser_returns403() throws Exception {
            mockMvc.perform(get("/users/" + regularUser.getId())
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 when admin user gets user by ID")
        void getUserByIdWithAdminUser_returns200() throws Exception {
            mockMvc.perform(get("/users/" + regularUser.getId())
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(regularUser.getId().toString()));
        }

        @Test
        @DisplayName("should return 403 when accessing admin endpoint without token")
        void adminEndpointWithoutToken_returns403() throws Exception {
            mockMvc.perform(get("/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Public Endpoints - No Authentication Required")
    class PublicEndpointTests {

        @Test
        @DisplayName("should allow access to user count endpoint without authentication")
        void userCountEndpoint_accessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/users/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").exists());
        }

        @Test
        @DisplayName("should require authentication for posts endpoint")
        void postsEndpoint_requiresAuth() throws Exception {
            mockMvc.perform(get("/posts"))
                    .andExpect(status().isForbidden());
        }
    }
}
