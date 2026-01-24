package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.dto.CollectionCreateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookmarkController covering bookmark and collection operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BookmarkController Integration Tests")
class BookmarkControllerIntegrationTest {

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
    private BookmarkCollectionRepository bookmarkCollectionRepository;

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
        bookmarkCollectionRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "bookmark@example.com",
                "bookmarkuser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        testUser = userRepository.save(testUser);
        userToken = jwtIssuer.issue(testUser.getId(), testUser.getUsername(), "ROLE_USER");

        testPost = new Post("BookmarkTest", "Test post for bookmarks", null, testUser);
        testPost = postRepository.save(testPost);
    }

    @Nested
    @DisplayName("POST /bookmarks/posts/{postId}")
    class CreateBookmarkTests {

        @Test
        @DisplayName("should return 201 when bookmarking a post")
        void createBookmark_success_returns201() throws Exception {
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.post.id").value(testPost.getId().toString()));
        }

        @Test
        @DisplayName("should return 200 when bookmarking already bookmarked post (idempotent)")
        void createBookmark_duplicate_returns200() throws Exception {
            // First bookmark
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated());

            // Bookmark again
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 for non-existent post")
        void createBookmark_notFound_returns404() throws Exception {
            mockMvc.perform(post("/bookmarks/posts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void createBookmark_noAuth_returns403() throws Exception {
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /bookmarks/posts/{postId}")
    class DeleteBookmarkTests {

        @Test
        @DisplayName("should return 204 when removing bookmark")
        void deleteBookmark_success_returns204() throws Exception {
            // First bookmark
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated());

            // Then remove
            mockMvc.perform(delete("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should be idempotent - removing non-bookmarked returns 204")
        void deleteBookmark_notBookmarked_returns204() throws Exception {
            mockMvc.perform(delete("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /bookmarks")
    class GetBookmarksTests {

        @Test
        @DisplayName("should return 200 with empty list initially")
        void getBookmarks_empty_returns200() throws Exception {
            mockMvc.perform(get("/bookmarks")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should return 200 with bookmarks after creating")
        void getBookmarks_withBookmarks_returns200() throws Exception {
            // Create bookmark
            mockMvc.perform(post("/bookmarks/posts/" + testPost.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated());

            // Get bookmarks
            mockMvc.perform(get("/bookmarks")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void getBookmarks_noAuth_returns403() throws Exception {
            mockMvc.perform(get("/bookmarks"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /bookmarks/collections")
    class CreateCollectionTests {

        @Test
        @DisplayName("should return 201 when creating collection")
        void createCollection_success_returns201() throws Exception {
            CollectionCreateRequest request = new CollectionCreateRequest("My Collection", null, null, null);

            mockMvc.perform(post("/bookmarks/collections")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("My Collection"));
        }

        @Test
        @DisplayName("should return 409 when collection name already exists")
        void createCollection_duplicateName_returns409() throws Exception {
            CollectionCreateRequest request = new CollectionCreateRequest("Duplicate Collection", null, null, null);

            // Create first
            mockMvc.perform(post("/bookmarks/collections")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Create duplicate
            mockMvc.perform(post("/bookmarks/collections")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void createCollection_noAuth_returns403() throws Exception {
            CollectionCreateRequest request = new CollectionCreateRequest("Test Collection", null, null, null);

            mockMvc.perform(post("/bookmarks/collections")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /bookmarks/collections")
    class GetCollectionsTests {

        @Test
        @DisplayName("should return 200 with empty list initially")
        void getCollections_empty_returns200() throws Exception {
            mockMvc.perform(get("/bookmarks/collections")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("DELETE /bookmarks/collections/{id}")
    class DeleteCollectionTests {

        @Test
        @DisplayName("should return 404 for non-existent collection")
        void deleteCollection_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/bookmarks/collections/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }
    }
}
