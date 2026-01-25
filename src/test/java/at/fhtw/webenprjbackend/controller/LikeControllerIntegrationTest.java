package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Post;
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
 * Integration tests for LikeController covering post like/unlike operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("LikeController Integration Tests")
class LikeControllerIntegrationTest {

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

    private User testUser;
    private Post testPost;
    private String userToken;

    @BeforeEach
    void setUp() {
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "liker@example.com",
                "likeruser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        testUser = userRepository.save(testUser);
        userToken = jwtIssuer.issue(testUser.getId(), testUser.getUsername(), "ROLE_USER");

        testPost = new Post("TestSubject", "Test post content for likes", null, testUser);
        testPost = postRepository.save(testPost);
    }

    @Nested
    @DisplayName("POST /posts/{id}/like")
    class LikePostTests {

        @Test
        @DisplayName("should return 204 when liking a post")
        void likePost_success_returns204() throws Exception {
            mockMvc.perform(post("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should be idempotent - liking twice returns 204")
        void likePost_twice_returns204() throws Exception {
            mockMvc.perform(post("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());

            // Like again - should still succeed
            mockMvc.perform(post("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void likePost_notFound_returns404() throws Exception {
            mockMvc.perform(post("/posts/" + UUID.randomUUID() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void likePost_noAuth_returns403() throws Exception {
            mockMvc.perform(post("/posts/" + testPost.getId() + "/like"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /posts/{id}/like")
    class UnlikePostTests {

        @Test
        @DisplayName("should return 204 when unliking a post")
        void unlikePost_success_returns204() throws Exception {
            // First like
            mockMvc.perform(post("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());

            // Then unlike
            mockMvc.perform(delete("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should be idempotent - unliking non-liked post returns 204")
        void unlikePost_notLiked_returns204() throws Exception {
            mockMvc.perform(delete("/posts/" + testPost.getId() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void unlikePost_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/posts/" + UUID.randomUUID() + "/like")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void unlikePost_noAuth_returns403() throws Exception {
            mockMvc.perform(delete("/posts/" + testPost.getId() + "/like"))
                    .andExpect(status().isForbidden());
        }
    }
}
