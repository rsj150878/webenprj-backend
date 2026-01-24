package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.dto.CredentialChangeRequests;
import at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.*;
import at.fhtw.webenprjbackend.security.jwt.JwtIssuer;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController covering user management operations.
 * Uses real Spring context with H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

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
    private JwtIssuer jwtIssuer;

    private static final String TEST_EMAIL = "test.user@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Password123!";

    private User testUser;
    private User adminUser;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up in order of foreign key dependencies
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                TEST_EMAIL,
                TEST_USERNAME,
                passwordEncoder.encode(TEST_PASSWORD),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        testUser = userRepository.save(testUser);
        userToken = jwtIssuer.issue(testUser.getId(), testUser.getUsername(), "ROLE_USER");

        adminUser = new User(
                "admin@example.com",
                "adminuser",
                passwordEncoder.encode(TEST_PASSWORD),
                "AT",
                "/avatar-placeholder.svg",
                Role.ADMIN
        );
        adminUser = userRepository.save(adminUser);
        adminToken = jwtIssuer.issue(adminUser.getId(), adminUser.getUsername(), "ROLE_ADMIN");
    }

    @Nested
    @DisplayName("POST /users (Registration)")
    class RegistrationTests {

        @Test
        @DisplayName("should return 201 when registering with valid data")
        void registerUser_validData_returns201() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "newuser@example.com",
                    "newuser123",
                    "NewPassword123!",
                    "DE"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.username").value("newuser123"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void registerUser_emailExists_returns409() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    TEST_EMAIL, // existing email
                    "differentuser",
                    "NewPassword123!",
                    "DE"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 409 when username already exists")
        void registerUser_usernameExists_returns409() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "different@example.com",
                    TEST_USERNAME, // existing username
                    "NewPassword123!",
                    "DE"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 400 when password is too weak")
        void registerUser_weakPassword_returns400() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "weakpwd@example.com",
                    "weakpwduser",
                    "password", // no uppercase or digit
                    "DE"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when username is too short")
        void registerUser_shortUsername_returns400() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "short@example.com",
                    "usr", // too short (min 5)
                    "Password123!",
                    "DE"
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when country code is invalid")
        void registerUser_invalidCountryCode_returns400() throws Exception {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "invalid@example.com",
                    "validuser123",
                    "Password123!",
                    "XX" // invalid country code
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /users/me (Current User)")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return 200 with current user data")
        void getCurrentUser_authenticated_returns200() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.countryCode").value("AT"));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void getCurrentUser_notAuthenticated_returns401() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/me (Update Profile)")
    class UpdateProfileTests {

        @Test
        @DisplayName("should return 200 when updating profile with valid data")
        void updateProfile_validData_returns200() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    TEST_EMAIL, // email is required
                    "updateduser",
                    "DE"
            );

            mockMvc.perform(put("/users/me")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.username").value("updateduser"))
                    .andExpect(jsonPath("$.user.countryCode").value("DE"));
        }

        @Test
        @DisplayName("should return 400 when updating with invalid username")
        void updateProfile_invalidUsername_returns400() throws Exception {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    TEST_EMAIL,
                    "ab", // too short
                    "AT"
            );

            mockMvc.perform(put("/users/me")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /users/me/password (Change Password)")
    class ChangePasswordTests {

        @Test
        @DisplayName("should return 204 when changing password with valid data")
        void changePassword_validData_returns204() throws Exception {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange();
            request.setCurrentPassword(TEST_PASSWORD);
            request.setNewPassword("NewPassword456!");

            mockMvc.perform(patch("/users/me/password")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 400 when current password is incorrect")
        void changePassword_wrongCurrentPassword_returns400() throws Exception {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange();
            request.setCurrentPassword("WrongPassword123!");
            request.setNewPassword("NewPassword456!");

            mockMvc.perform(patch("/users/me/password")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /users/count (User Count)")
    class UserCountTests {

        @Test
        @DisplayName("should return user count without authentication")
        void getUserCount_noAuth_returns200() throws Exception {
            mockMvc.perform(get("/users/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2)); // testUser and adminUser
        }
    }

    @Nested
    @DisplayName("Admin Operations")
    class AdminOperationsTests {

        @Test
        @DisplayName("should return all users for admin")
        void getAllUsers_asAdmin_returns200() throws Exception {
            mockMvc.perform(get("/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should return 403 for non-admin getting all users")
        void getAllUsers_asUser_returns403() throws Exception {
            mockMvc.perform(get("/users")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return user by ID for admin")
        void getUserById_asAdmin_returns200() throws Exception {
            mockMvc.perform(get("/users/" + testUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL));
        }

        @Test
        @DisplayName("should return 404 for non-existent user")
        void getUserById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/users/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should toggle user active status as admin")
        void toggleActiveStatus_asAdmin_returns200() throws Exception {
            mockMvc.perform(patch("/users/" + testUser.getId() + "/active")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("active", "false"))
                    .andExpect(status().isOk());

            User updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.isActive()).isFalse();
        }

        @Test
        @DisplayName("should delete user as admin")
        void deleteUser_asAdmin_returns204() throws Exception {
            User toDelete = new User(
                    "todelete@example.com",
                    "todelete",
                    passwordEncoder.encode(TEST_PASSWORD),
                    "AT",
                    "/avatar-placeholder.svg",
                    Role.USER
            );
            toDelete = userRepository.save(toDelete);

            mockMvc.perform(delete("/users/" + toDelete.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            assertThat(userRepository.findById(toDelete.getId())).isEmpty();
        }

        @Test
        @DisplayName("should return 403 when non-admin tries to delete user")
        void deleteUser_asUser_returns403() throws Exception {
            mockMvc.perform(delete("/users/" + adminUser.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }
}
