package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.PostLikeRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PostService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private BookmarkService bookmarkService;

    private PostService postService;

    private User testUser;
    private UUID testUserId;
    private Post testPost;
    private UUID testPostId;

    @BeforeEach
    void setUp() {
        postService = new PostService(
                postRepository,
                userRepository,
                postLikeRepository,
                followRepository,
                bookmarkService
        );

        testUserId = UUID.randomUUID();
        testUser = createTestUser(testUserId, "testuser", "test@example.com");

        testPostId = UUID.randomUUID();
        testPost = createTestPost(testPostId, "webdev", "Learning Spring Boot!", testUser);
    }

    private User createTestUser(UUID id, String username, String email) {
        User user = new User(email, username, "hashedPassword", "AT",
                "https://example.com/profile.png", "Dr.", Role.USER);
        setField(user, "id", id);
        setField(user, "createdAt", LocalDateTime.now());
        return user;
    }

    private Post createTestPost(UUID id, String subject, String content, User user) {
        Post post = new Post(subject, content, null, user);
        setField(post, "id", id);
        setField(post, "createdAt", LocalDateTime.now());
        return post;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private void setupMocksForMapping(List<Post> posts) {
        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        when(postLikeRepository.countLikesByPostIds(postIds)).thenReturn(Collections.emptyList());
        when(postLikeRepository.findLikedPostIds(any(), eq(postIds))).thenReturn(Collections.emptyList());
        when(bookmarkService.fetchBookmarkCounts(posts)).thenReturn(Collections.emptyMap());
        when(bookmarkService.fetchBookmarkedPostIds(eq(posts), any())).thenReturn(Collections.emptySet());
        when(postRepository.countCommentsByParentIds(postIds)).thenReturn(Collections.emptyList());
    }

    @Nested
    @DisplayName("getAllPosts()")
    class GetAllPostsTests {

        @Test
        @DisplayName("should return paginated posts")
        void getAllPosts_returnsPaginatedPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.getAllPosts(pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            // PostResponse is a record - use accessor methods without "get" prefix
            assertThat(result.getContent().get(0).subject()).isEqualTo("#webdev");
            assertThat(result.getContent().get(0).content()).isEqualTo("Learning Spring Boot!");

            verify(postRepository).findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("should return empty page when no posts exist")
        void getAllPosts_noPosts_returnsEmptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(emptyPage);

            // Act
            Page<PostResponse> result = postService.getAllPosts(pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getPostById()")
    class GetPostByIdTests {

        @Test
        @DisplayName("should return post when found")
        void getPostById_found_returnsPost() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            setupMocksForMapping(List.of(testPost));

            // Act
            PostResponse result = postService.getPostById(testPostId, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testPostId);
            assertThat(result.subject()).isEqualTo("#webdev");
            assertThat(result.content()).isEqualTo("Learning Spring Boot!");
            assertThat(result.username()).isEqualTo("testuser");

            verify(postRepository).findById(testPostId);
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void getPostById_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.getPostById(nonExistentId, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }
    }

    @Nested
    @DisplayName("createPost()")
    class CreatePostTests {

        @Test
        @DisplayName("should create post successfully")
        void createPost_success() {
            // Arrange
            PostCreateRequest request = new PostCreateRequest("java", "Learning Java is fun!");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post saved = invocation.getArgument(0);
                setField(saved, "id", UUID.randomUUID());
                setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });

            // Setup mocks for the mapping after save
            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).countLikesByPostIds(any());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).findLikedPostIds(any(), any());
            when(bookmarkService.fetchBookmarkCounts(any())).thenReturn(Collections.emptyMap());
            when(bookmarkService.fetchBookmarkedPostIds(any(), any())).thenReturn(Collections.emptySet());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postRepository).countCommentsByParentIds(any());

            // Act
            PostResponse result = postService.createPost(request, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.subject()).isEqualTo("#java");
            assertThat(result.content()).isEqualTo("Learning Java is fun!");

            verify(userRepository).findById(testUserId);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("should normalize subject by removing leading #")
        void createPost_normalizesSubject() {
            // Arrange
            PostCreateRequest request = new PostCreateRequest("#typescript", "TypeScript is great!");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post saved = invocation.getArgument(0);
                // Verify subject was normalized (# removed)
                assertThat(saved.getSubject()).isEqualTo("typescript");
                setField(saved, "id", UUID.randomUUID());
                setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });

            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).countLikesByPostIds(any());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).findLikedPostIds(any(), any());
            when(bookmarkService.fetchBookmarkCounts(any())).thenReturn(Collections.emptyMap());
            when(bookmarkService.fetchBookmarkedPostIds(any(), any())).thenReturn(Collections.emptySet());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postRepository).countCommentsByParentIds(any());

            // Act
            postService.createPost(request, testUserId);

            // Assert
            verify(postRepository).save(argThat(post -> post.getSubject().equals("typescript")));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void createPost_userNotFound_throwsException() {
            // Arrange
            PostCreateRequest request = new PostCreateRequest("test", "Test content");

            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.createPost(request, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");

            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create comment when parentId is provided")
        void createPost_withParentId_createsComment() {
            // Arrange
            UUID parentId = UUID.randomUUID();
            Post parentPost = createTestPost(parentId, "parent", "Parent post", testUser);

            PostCreateRequest request = new PostCreateRequest("reply", "This is a reply!");
            request.setParentId(parentId);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.findById(parentId)).thenReturn(Optional.of(parentPost));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post saved = invocation.getArgument(0);
                assertThat(saved.getParent()).isEqualTo(parentPost);
                setField(saved, "id", UUID.randomUUID());
                setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });

            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).countLikesByPostIds(any());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postLikeRepository).findLikedPostIds(any(), any());
            when(bookmarkService.fetchBookmarkCounts(any())).thenReturn(Collections.emptyMap());
            when(bookmarkService.fetchBookmarkedPostIds(any(), any())).thenReturn(Collections.emptySet());
            doAnswer(invocation -> Collections.emptyList())
                    .when(postRepository).countCommentsByParentIds(any());

            // Act
            PostResponse result = postService.createPost(request, testUserId);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findById(parentId);
        }
    }

    @Nested
    @DisplayName("deletePost()")
    class DeletePostTests {

        @Test
        @DisplayName("should soft delete post by setting active to false")
        void deletePost_success() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            // Act
            postService.deletePost(testPostId);

            // Assert
            verify(postRepository).findById(testPostId);
            verify(postRepository).save(argThat(post -> !post.isActive()));
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void deletePost_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.deletePost(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");

            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("searchPosts()")
    class SearchPostsTests {

        @Test
        @DisplayName("should search posts by keyword")
        void searchPosts_withKeyword_returnsMatchingPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueAndContentContainingIgnoreCase("Spring", pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.searchPosts("Spring", pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return all posts when keyword is null")
        void searchPosts_nullKeyword_returnsAllPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.searchPosts(null, pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("should return all posts when keyword is empty")
        void searchPosts_emptyKeyword_returnsAllPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.searchPosts("   ", pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable);
        }
    }
}
