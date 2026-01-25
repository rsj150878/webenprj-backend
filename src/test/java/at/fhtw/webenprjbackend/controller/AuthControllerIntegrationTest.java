package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.dto.LoginRequest;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.*;
import at.fhtw.webenprjbackend.security.ratelimit.RateLimitingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController covering authentication flow and rate limiting.
 * Uses real Spring context with H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private RateLimitingFilter rateLimitingFilter;

    private static final String TEST_EMAIL = "test.user@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        rateLimitingFilter.clearAttempts();
        // Clean up in order of foreign key dependencies
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User(
                TEST_EMAIL,
                TEST_USERNAME,
                passwordEncoder.encode(TEST_PASSWORD),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("should return 200 and token when login with valid email")
        void loginWithEmail_success() throws Exception {
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.user.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.user.role").value("USER"))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("token");
        }

        @Test
        @DisplayName("should return 200 and token when login with valid username")
        void loginWithUsername_success() throws Exception {
            LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.user.username").value(TEST_USERNAME));
        }

        @Test
        @DisplayName("should return 401 when login with invalid password")
        void loginWithInvalidPassword_returns401() throws Exception {
            LoginRequest request = new LoginRequest(TEST_EMAIL, "WrongPassword123!");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 when login with non-existent user")
        void loginWithNonExistentUser_returns401() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when login with missing credentials")
        void loginWithMissingCredentials_returns400() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when login identifier is too short")
        void loginWithShortIdentifier_returns400() throws Exception {
            LoginRequest request = new LoginRequest("ab", TEST_PASSWORD);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void loginWithShortPassword_returns400() throws Exception {
            LoginRequest request = new LoginRequest(TEST_EMAIL, "short");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Rate Limiting")
    class RateLimitingTests {

        @Test
        @DisplayName("should return 429 after exceeding rate limit")
        void loginExceedsRateLimit_returns429() throws Exception {
            LoginRequest request = new LoginRequest(TEST_EMAIL, "WrongPassword!");

            // Make 6 requests (limit is 5)
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }

            // 6th request should be rate limited
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("Admin Login")
    class AdminLoginTests {

        @BeforeEach
        void setUpAdmin() {
            User adminUser = new User(
                    "admin@example.com",
                    "adminuser",
                    passwordEncoder.encode(TEST_PASSWORD),
                    "AT",
                    "/avatar-placeholder.svg",
                    Role.ADMIN
            );
            userRepository.save(adminUser);
        }

        @Test
        @DisplayName("should return ADMIN role for admin user login")
        void loginAsAdmin_returnsAdminRole() throws Exception {
            LoginRequest request = new LoginRequest("admin@example.com", TEST_PASSWORD);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.user.role").value("ADMIN"));
        }
    }
}
