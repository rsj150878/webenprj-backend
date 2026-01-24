package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.entity.Post;
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
 * Integration tests for PostController covering post CRUD operations.
 * Uses real Spring context with H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PostController Integration Tests")
class PostControllerIntegrationTest {

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

    private static final String TEST_PASSWORD = "Password123!";

    private User testUser;
    private User otherUser;
    private User adminUser;
    private String userToken;
    private String otherUserToken;
    private String adminToken;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Clean up in order of foreign key dependencies
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "poster@example.com",
                "postuser",
                passwordEncoder.encode(TEST_PASSWORD),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        testUser = userRepository.save(testUser);
        userToken = jwtIssuer.issue(testUser.getId(), testUser.getUsername(), "ROLE_USER");

        otherUser = new User(
                "other@example.com",
                "otheruser",
                passwordEncoder.encode(TEST_PASSWORD),
                "DE",
                "/avatar-placeholder.svg",
                Role.USER
        );
        otherUser = userRepository.save(otherUser);
        otherUserToken = jwtIssuer.issue(otherUser.getId(), otherUser.getUsername(), "ROLE_USER");

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

        testPost = new Post("StudyTips", "Learning Java Spring Boot is fun!", null, testUser);
        testPost = postRepository.save(testPost);
    }

    @Nested
    @DisplayName("GET /posts")
    class GetAllPostsTests {

        @Test
        @DisplayName("should return 200 with all posts")
        void getAllPosts_authenticated_returns200() throws Exception {
            mockMvc.perform(get("/posts")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].subject").value("#StudyTips")); // Service adds # prefix
        }

        @Test
        @DisplayName("should return 403 when not authenticated")
        void getAllPosts_notAuthenticated_returns403() throws Exception {
            mockMvc.perform(get("/posts"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should filter posts by subject")
        void getAllPosts_filterBySubject_returns200() throws Exception {
            Post anotherPost = new Post("#JavaBasics", "Learning about classes", null, testUser);
            postRepository.save(anotherPost);

            mockMvc.perform(get("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .param("subject", "#StudyTips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /posts/{id}")
    class GetPostByIdTests {

        @Test
        @DisplayName("should return 200 with post data")
        void getPostById_exists_returns200() throws Exception {
            mockMvc.perform(get("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testPost.getId().toString()))
                    .andExpect(jsonPath("$.subject").value("#StudyTips")) // Service adds # prefix
                    .andExpect(jsonPath("$.content").value("Learning Java Spring Boot is fun!"));
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void getPostById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /posts")
    class CreatePostTests {

        @Test
        @DisplayName("should return 201 when creating post with valid data")
        void createPost_validData_returns201() throws Exception {
            PostCreateRequest request = new PostCreateRequest("#NewTopic", "This is my new study post content!");

            mockMvc.perform(post("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.subject").value("#NewTopic"))
                    .andExpect(jsonPath("$.content").value("This is my new study post content!"))
                    .andExpect(jsonPath("$.username").value("postuser"));
        }

        @Test
        @DisplayName("should return 400 when subject is missing")
        void createPost_missingSubject_returns400() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setContent("Content without subject");

            mockMvc.perform(post("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when content is too short")
        void createPost_shortContent_returns400() throws Exception {
            PostCreateRequest request = new PostCreateRequest("#Topic", "Hi"); // less than 5 chars

            mockMvc.perform(post("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when subject is too long")
        void createPost_longSubject_returns400() throws Exception {
            PostCreateRequest request = new PostCreateRequest(
                    "#ThisSubjectIsTooLongForTheValidation12345",
                    "Valid content here"
            );

            mockMvc.perform(post("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should create comment when parentId is provided")
        void createPost_withParentId_createsComment() throws Exception {
            PostCreateRequest request = new PostCreateRequest("#Reply", "This is a reply to the post");
            request.setParentId(testPost.getId());

            mockMvc.perform(post("/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.parentId").value(testPost.getId().toString()));
        }

        @Test
        @DisplayName("should return 403 when not authenticated")
        void createPost_notAuthenticated_returns403() throws Exception {
            PostCreateRequest request = new PostCreateRequest("#Topic", "Content here");

            mockMvc.perform(post("/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /posts/{id}")
    class UpdatePostTests {

        @Test
        @DisplayName("should return 200 when owner updates post")
        void updatePost_asOwner_returns200() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setContent("Updated content for my study post");

            mockMvc.perform(put("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Updated content for my study post"));
        }

        @Test
        @DisplayName("should return 200 when admin updates any post")
        void updatePost_asAdmin_returns200() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setContent("Admin updated this content");

            mockMvc.perform(put("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Admin updated this content"));
        }

        @Test
        @DisplayName("should return 403 when non-owner tries to update post")
        void updatePost_asNonOwner_returns403() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setContent("Trying to update someone else's post");

            mockMvc.perform(put("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + otherUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void updatePost_notFound_returns404() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setContent("Update content");

            mockMvc.perform(put("/posts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /posts/{id}")
    class DeletePostTests {

        @Test
        @DisplayName("should return 204 when owner deletes post")
        void deletePost_asOwner_returns204() throws Exception {
            mockMvc.perform(delete("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());

            // Verify post is soft-deleted (active = false)
            Post deleted = postRepository.findById(testPost.getId()).orElseThrow();
            assertThat(deleted.isActive()).isFalse();
        }

        @Test
        @DisplayName("should return 204 when admin deletes any post")
        void deletePost_asAdmin_returns204() throws Exception {
            mockMvc.perform(delete("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 403 when non-owner tries to delete post")
        void deletePost_asNonOwner_returns403() throws Exception {
            mockMvc.perform(delete("/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + otherUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void deletePost_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/posts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /posts/{id}/comments")
    class GetCommentsTests {

        @Test
        @DisplayName("should return 200 with comments for a post")
        void getComments_returns200() throws Exception {
            // Create a comment on the test post
            Post comment = new Post("#Reply", "This is a comment", null, otherUser);
            comment.setParent(testPost);
            postRepository.save(comment);

            mockMvc.perform(get("/posts/" + testPost.getId() + "/comments")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should return 404 for non-existent post comments")
        void getComments_postNotFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/" + UUID.randomUUID() + "/comments")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /posts/subjects")
    class GetSubjectsTests {

        @Test
        @DisplayName("should return 200 with available subjects")
        void getSubjects_returns200() throws Exception {
            mockMvc.perform(get("/posts/subjects")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}
