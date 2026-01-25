package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.*;
import at.fhtw.webenprjbackend.security.jwt.JwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FollowController covering follow/unfollow operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("FollowController Integration Tests")
class FollowControllerIntegrationTest {

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
    private JwtIssuer jwtIssuer;

    private User followerUser;
    private User targetUser;
    private String followerToken;

    @BeforeEach
    void setUp() {
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        followerUser = new User(
                "follower@example.com",
                "followeruser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        followerUser = userRepository.save(followerUser);
        followerToken = jwtIssuer.issue(followerUser.getId(), followerUser.getUsername(), "ROLE_USER");

        targetUser = new User(
                "target@example.com",
                "targetuser",
                passwordEncoder.encode("Password123!"),
                "DE",
                "/avatar-placeholder.svg",
                Role.USER
        );
        targetUser = userRepository.save(targetUser);
    }

    @Nested
    @DisplayName("POST /users/{id}/follow")
    class FollowUserTests {

        @Test
        @DisplayName("should return 204 when following a user")
        void followUser_success_returns204() throws Exception {
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should be idempotent - following twice returns 204")
        void followUser_twice_returns204() throws Exception {
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 for non-existent user")
        void followUser_notFound_returns404() throws Exception {
            mockMvc.perform(post("/users/" + UUID.randomUUID() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void followUser_noAuth_returns403() throws Exception {
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}/follow")
    class UnfollowUserTests {

        @Test
        @DisplayName("should return 204 when unfollowing a user")
        void unfollowUser_success_returns204() throws Exception {
            // First follow
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());

            // Then unfollow
            mockMvc.perform(delete("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should be idempotent - unfollowing non-followed user returns 204")
        void unfollowUser_notFollowed_returns204() throws Exception {
            mockMvc.perform(delete("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 for non-existent user")
        void unfollowUser_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/users/" + UUID.randomUUID() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /users/{id}/followers")
    class GetFollowersTests {

        @Test
        @DisplayName("should return 200 with empty list initially")
        void getFollowers_empty_returns200() throws Exception {
            mockMvc.perform(get("/users/" + targetUser.getId() + "/followers")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should return 200 with followers list after following")
        void getFollowers_withFollowers_returns200() throws Exception {
            // Follow first
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());

            // Get followers
            mockMvc.perform(get("/users/" + targetUser.getId() + "/followers")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /users/{id}/following")
    class GetFollowingTests {

        @Test
        @DisplayName("should return 200 with empty list initially")
        void getFollowing_empty_returns200() throws Exception {
            mockMvc.perform(get("/users/" + followerUser.getId() + "/following")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /users/{id}/following-status")
    class GetFollowingStatusTests {

        @Test
        @DisplayName("should return false when not following")
        void followingStatus_notFollowing_returnsFalse() throws Exception {
            mockMvc.perform(get("/users/" + targetUser.getId() + "/following-status")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.following").value(false));
        }

        @Test
        @DisplayName("should return true when following")
        void followingStatus_following_returnsTrue() throws Exception {
            // Follow first
            mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isNoContent());

            // Check status
            mockMvc.perform(get("/users/" + targetUser.getId() + "/following-status")
                            .header("Authorization", "Bearer " + followerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.following").value(true));
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void followingStatus_noAuth_returns403() throws Exception {
            mockMvc.perform(get("/users/" + targetUser.getId() + "/following-status"))
                    .andExpect(status().isForbidden());
        }
    }
}
