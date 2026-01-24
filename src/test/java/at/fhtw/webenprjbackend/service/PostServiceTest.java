package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.AdminPostResponse;
import at.fhtw.webenprjbackend.dto.AdminPostStatsResponse;
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

    @Nested
    @DisplayName("getFollowingPosts()")
    class GetFollowingPostsTests {

        @Test
        @DisplayName("should return empty page when user follows no one")
        void getFollowingPosts_noFollowing_returnsEmpty() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(followRepository.findByFollower(eq(testUser), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            Page<PostResponse> result = postService.getFollowingPosts(pageable, testUserId);

            // Assert
            assertThat(result).isEmpty();
            verify(postRepository, never()).findByParentIsNullAndActiveTrueAndUserIdInOrderByCreatedAtDesc(any(), any());
        }

        @Test
        @DisplayName("should throw exception when user is null")
        void getFollowingPosts_nullUser_throwsUnauthorized() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act & Assert
            assertThatThrownBy(() -> postService.getFollowingPosts(pageable, null))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Authentication required");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getFollowingPosts_userNotFound_throwsNotFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.getFollowingPosts(pageable, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getCommentsForPost()")
    class GetCommentsForPostTests {

        @Test
        @DisplayName("should return comments for existing post")
        void getCommentsForPost_postExists_returnsComments() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Post comment = createTestPost(UUID.randomUUID(), "reply", "This is a comment", testUser);
            List<Post> comments = List.of(comment);
            Page<Post> commentPage = new PageImpl<>(comments, pageable, 1);

            when(postRepository.existsById(testPostId)).thenReturn(true);
            when(postRepository.findByParentIdAndActiveTrueOrderByCreatedAtAsc(testPostId, pageable))
                    .thenReturn(commentPage);
            setupMocksForMapping(comments);

            // Act
            Page<PostResponse> result = postService.getCommentsForPost(testPostId, pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void getCommentsForPost_postNotFound_throwsException() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.existsById(nonExistentId)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> postService.getCommentsForPost(nonExistentId, pageable, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }
    }

    @Nested
    @DisplayName("updatePost()")
    class UpdatePostTests {

        @Test
        @DisplayName("should update subject only")
        void updatePost_subjectOnly_updatesSubject() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            // Setup minimal mocks for mapping (updatePost passes null for currentUserId)
            when(postLikeRepository.countLikesByPostIds(any())).thenReturn(Collections.emptyList());
            when(bookmarkService.fetchBookmarkCounts(any())).thenReturn(Collections.emptyMap());
            when(postRepository.countCommentsByParentIds(any())).thenReturn(Collections.emptyList());

            at.fhtw.webenprjbackend.dto.PostUpdateRequest request =
                    new at.fhtw.webenprjbackend.dto.PostUpdateRequest();
            request.setSubject("newsubject");

            // Act
            PostResponse result = postService.updatePost(testPostId, request);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).save(argThat(post -> post.getSubject().equals("newsubject")));
        }

        @Test
        @DisplayName("should update content only")
        void updatePost_contentOnly_updatesContent() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            // Setup minimal mocks for mapping (updatePost passes null for currentUserId)
            when(postLikeRepository.countLikesByPostIds(any())).thenReturn(Collections.emptyList());
            when(bookmarkService.fetchBookmarkCounts(any())).thenReturn(Collections.emptyMap());
            when(postRepository.countCommentsByParentIds(any())).thenReturn(Collections.emptyList());

            at.fhtw.webenprjbackend.dto.PostUpdateRequest request =
                    new at.fhtw.webenprjbackend.dto.PostUpdateRequest();
            request.setContent("Updated content here");

            // Act
            PostResponse result = postService.updatePost(testPostId, request);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).save(argThat(post -> post.getContent().equals("Updated content here")));
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void updatePost_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            at.fhtw.webenprjbackend.dto.PostUpdateRequest request =
                    new at.fhtw.webenprjbackend.dto.PostUpdateRequest();
            request.setSubject("test");

            // Act & Assert
            assertThatThrownBy(() -> postService.updatePost(nonExistentId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }
    }

    @Nested
    @DisplayName("searchBySubject()")
    class SearchBySubjectTests {

        @Test
        @DisplayName("should find posts by subject")
        void searchBySubject_found_returnsPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueAndSubjectIgnoreCase("webdev", pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.searchBySubject("webdev", pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should normalize subject with hashtag")
        void searchBySubject_withHashtag_normalizesSubject() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findByParentIsNullAndActiveTrueAndSubjectIgnoreCase("java", pageable))
                    .thenReturn(emptyPage);

            // Act
            postService.searchBySubject("#java", pageable, testUserId);

            // Assert - verify # was stripped
            verify(postRepository).findByParentIsNullAndActiveTrueAndSubjectIgnoreCase("java", pageable);
        }
    }

    @Nested
    @DisplayName("getPostsByAuthor()")
    class GetPostsByAuthorTests {

        @Test
        @DisplayName("should return posts by author")
        void getPostsByAuthor_found_returnsPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveTrueAndUserIdOrderByCreatedAtDesc(testUserId, pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.getPostsByAuthor(testUserId, pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when author has no posts")
        void getPostsByAuthor_noPosts_returnsEmpty() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            UUID authorId = UUID.randomUUID();
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findByParentIsNullAndActiveTrueAndUserIdOrderByCreatedAtDesc(authorId, pageable))
                    .thenReturn(emptyPage);

            // Act
            Page<PostResponse> result = postService.getPostsByAuthor(authorId, pageable, testUserId);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAvailableSubjects()")
    class GetAvailableSubjectsTests {

        @Test
        @DisplayName("should return subjects with hashtag prefix")
        void getAvailableSubjects_returnsWithHashtags() {
            // Arrange
            when(postRepository.findDistinctSubjects()).thenReturn(List.of("java", "spring", "webdev"));

            // Act
            List<String> result = postService.getAvailableSubjects();

            // Assert
            assertThat(result).containsExactly("#java", "#spring", "#webdev");
        }

        @Test
        @DisplayName("should return empty list when no subjects")
        void getAvailableSubjects_empty_returnsEmpty() {
            // Arrange
            when(postRepository.findDistinctSubjects()).thenReturn(Collections.emptyList());

            // Act
            List<String> result = postService.getAvailableSubjects();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasUserPostedToday()")
    class HasUserPostedTodayTests {

        @Test
        @DisplayName("should return true when user posted today")
        void hasUserPostedToday_posted_returnsTrue() {
            // Arrange
            when(postRepository.existsByUserIdAndParentIsNullAndActiveTrueAndCreatedAtGreaterThanEqual(
                    eq(testUserId), any())).thenReturn(true);

            // Act
            boolean result = postService.hasUserPostedToday(testUserId);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when user has not posted today")
        void hasUserPostedToday_notPosted_returnsFalse() {
            // Arrange
            when(postRepository.existsByUserIdAndParentIsNullAndActiveTrueAndCreatedAtGreaterThanEqual(
                    eq(testUserId), any())).thenReturn(false);

            // Act
            boolean result = postService.hasUserPostedToday(testUserId);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getUserActivity()")
    class GetUserActivityTests {

        @Test
        @DisplayName("should return all user activity")
        void getUserActivity_returnsPostsAndComments() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(testUserId, pageable))
                    .thenReturn(postPage);
            setupMocksForMapping(posts);

            // Act
            Page<PostResponse> result = postService.getUserActivity(testUserId, pageable, testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getPostCount()")
    class GetPostCountTests {

        @Test
        @DisplayName("should return total post count")
        void getPostCount_returnsCount() {
            // Arrange
            when(postRepository.count()).thenReturn(42L);

            // Act
            long result = postService.getPostCount();

            // Assert
            assertThat(result).isEqualTo(42L);
        }
    }


    @Nested
    @DisplayName("getAdminPostStats()")
    class GetAdminPostStatsTests {

        @Test
        @DisplayName("should return post and comment statistics")
        void getAdminPostStats_returnsStats() {
            // Arrange
            when(postRepository.countByParentIsNull()).thenReturn(100L);
            when(postRepository.countByParentIsNullAndActiveTrue()).thenReturn(80L);
            when(postRepository.countByParentIsNotNull()).thenReturn(50L);
            when(postRepository.countByParentIsNotNullAndActiveTrue()).thenReturn(45L);

            // Act
            AdminPostStatsResponse result = postService.getAdminPostStats();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalPosts()).isEqualTo(100L);
            assertThat(result.activePosts()).isEqualTo(80L);
            assertThat(result.deletedPosts()).isEqualTo(20L); // 100 - 80
            assertThat(result.totalComments()).isEqualTo(50L);
            assertThat(result.activeComments()).isEqualTo(45L);
            assertThat(result.deletedComments()).isEqualTo(5L); // 50 - 45
        }

        @Test
        @DisplayName("should handle zero counts")
        void getAdminPostStats_zeroCounts_returnsZeros() {
            // Arrange
            when(postRepository.countByParentIsNull()).thenReturn(0L);
            when(postRepository.countByParentIsNullAndActiveTrue()).thenReturn(0L);
            when(postRepository.countByParentIsNotNull()).thenReturn(0L);
            when(postRepository.countByParentIsNotNullAndActiveTrue()).thenReturn(0L);

            // Act
            AdminPostStatsResponse result = postService.getAdminPostStats();

            // Assert
            assertThat(result.totalPosts()).isZero();
            assertThat(result.activePosts()).isZero();
            assertThat(result.deletedPosts()).isZero();
            assertThat(result.totalComments()).isZero();
            assertThat(result.activeComments()).isZero();
            assertThat(result.deletedComments()).isZero();
        }
    }

    @Nested
    @DisplayName("adminGetAllPosts()")
    class AdminGetAllPostsTests {

        private void setupAdminMocksForMapping(List<Post> posts) {
            List<UUID> postIds = posts.stream().map(Post::getId).toList();
            when(postLikeRepository.countLikesByPostIds(postIds)).thenReturn(Collections.emptyList());
            when(bookmarkService.fetchBookmarkCounts(posts)).thenReturn(Collections.emptyMap());
            when(postRepository.countCommentsByParentIds(postIds)).thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("should return all posts when no filters")
        void adminGetAllPosts_noFilters_returnsAll() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findAllForAdmin(pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findAllForAdmin(pageable);
        }

        @Test
        @DisplayName("should filter by active=true")
        void adminGetAllPosts_activeTrue_returnsActivePosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByActiveOrderByCreatedAtDesc(true, pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(true, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByActiveOrderByCreatedAtDesc(true, pageable);
        }

        @Test
        @DisplayName("should filter by active=false")
        void adminGetAllPosts_activeFalse_returnsInactivePosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Post inactivePost = createTestPost(UUID.randomUUID(), "test", "Inactive post", testUser);
            inactivePost.setActive(false);
            List<Post> posts = List.of(inactivePost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByActiveOrderByCreatedAtDesc(false, pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(false, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByActiveOrderByCreatedAtDesc(false, pageable);
        }

        @Test
        @DisplayName("should filter by isComment=true (comments only)")
        void adminGetAllPosts_commentsOnly_returnsComments() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Post comment = createTestPost(UUID.randomUUID(), "reply", "A comment", testUser);
            setField(comment, "parent", testPost);
            List<Post> posts = List.of(comment);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNotNullOrderByCreatedAtDesc(pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, true, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNotNullOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("should filter by isComment=false (posts only)")
        void adminGetAllPosts_postsOnly_returnsPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullOrderByCreatedAtDesc(pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, false, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNullOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("should filter by isComment=true and active=true")
        void adminGetAllPosts_activeComments_returnsActiveComments() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Post comment = createTestPost(UUID.randomUUID(), "reply", "Active comment", testUser);
            setField(comment, "parent", testPost);
            List<Post> posts = List.of(comment);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNotNullAndActiveOrderByCreatedAtDesc(true, pageable))
                    .thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(true, true, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNotNullAndActiveOrderByCreatedAtDesc(true, pageable);
        }

        @Test
        @DisplayName("should filter by isComment=false and active=true")
        void adminGetAllPosts_activePosts_returnsActivePosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.findByParentIsNullAndActiveOrderByCreatedAtDesc(true, pageable))
                    .thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(true, false, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).findByParentIsNullAndActiveOrderByCreatedAtDesc(true, pageable);
        }

        @Test
        @DisplayName("should search posts with keyword")
        void adminGetAllPosts_withSearch_searchesPosts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.searchAllForAdmin("Spring", pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, null, "Spring", pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).searchAllForAdmin("Spring", pageable);
        }

        @Test
        @DisplayName("should trim search keyword")
        void adminGetAllPosts_withWhitespaceSearch_trimsKeyword() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            when(postRepository.searchAllForAdmin("java", pageable)).thenReturn(postPage);
            setupAdminMocksForMapping(posts);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, null, "  java  ", pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(postRepository).searchAllForAdmin("java", pageable);
        }

        @Test
        @DisplayName("should return empty page when no posts match")
        void adminGetAllPosts_noMatches_returnsEmptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findAllForAdmin(pageable)).thenReturn(emptyPage);

            // Act
            Page<AdminPostResponse> result = postService.adminGetAllPosts(null, null, null, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("adminToggleActive()")
    class AdminToggleActiveTests {

        private void setupAdminMocksForSingle(Post post) {
            List<Post> posts = List.of(post);
            List<UUID> postIds = posts.stream().map(Post::getId).toList();
            when(postLikeRepository.countLikesByPostIds(postIds)).thenReturn(Collections.emptyList());
            when(bookmarkService.fetchBookmarkCounts(posts)).thenReturn(Collections.emptyMap());
            when(postRepository.countCommentsByParentIds(postIds)).thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("should activate post")
        void adminToggleActive_toTrue_activatesPost() {
            // Arrange
            Post inactivePost = createTestPost(UUID.randomUUID(), "test", "Content", testUser);
            inactivePost.setActive(false);

            when(postRepository.findById(inactivePost.getId())).thenReturn(Optional.of(inactivePost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(inactivePost);

            // Act
            AdminPostResponse result = postService.adminToggleActive(inactivePost.getId(), true);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.active()).isTrue();
            verify(postRepository).save(argThat(Post::isActive));
        }

        @Test
        @DisplayName("should deactivate post")
        void adminToggleActive_toFalse_deactivatesPost() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(testPost);

            // Act
            AdminPostResponse result = postService.adminToggleActive(testPostId, false);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.active()).isFalse();
            verify(postRepository).save(argThat(post -> !post.isActive()));
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void adminToggleActive_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.adminToggleActive(nonExistentId, true))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");

            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("adminHardDeletePost()")
    class AdminHardDeletePostTests {

        @Test
        @DisplayName("should permanently delete post")
        void adminHardDeletePost_success() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

            // Act
            postService.adminHardDeletePost(testPostId);

            // Assert
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void adminHardDeletePost_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.adminHardDeletePost(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");

            verify(postRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("mapToAdminResponse() - via adminToggleActive")
    class MapToAdminResponseTests {

        private void setupAdminMocksForSingle(Post post) {
            List<Post> posts = List.of(post);
            List<UUID> postIds = posts.stream().map(Post::getId).toList();
            when(postLikeRepository.countLikesByPostIds(postIds)).thenReturn(Collections.emptyList());
            when(bookmarkService.fetchBookmarkCounts(posts)).thenReturn(Collections.emptyMap());
            when(postRepository.countCommentsByParentIds(postIds)).thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("should include user email in admin response")
        void mapToAdminResponse_includesUserEmail() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(testPost);

            // Act
            AdminPostResponse result = postService.adminToggleActive(testPostId, true);

            // Assert
            assertThat(result.userEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should set isComment=true for posts with parent")
        void mapToAdminResponse_withParent_setsIsComment() {
            // Arrange
            Post comment = createTestPost(UUID.randomUUID(), "reply", "Comment content", testUser);
            setField(comment, "parent", testPost);

            when(postRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(comment);

            // Act
            AdminPostResponse result = postService.adminToggleActive(comment.getId(), true);

            // Assert
            assertThat(result.isComment()).isTrue();
            assertThat(result.parentId()).isEqualTo(testPostId);
        }

        @Test
        @DisplayName("should set isComment=false for top-level posts")
        void mapToAdminResponse_withoutParent_setsIsCommentFalse() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(testPost);

            // Act
            AdminPostResponse result = postService.adminToggleActive(testPostId, true);

            // Assert
            assertThat(result.isComment()).isFalse();
            assertThat(result.parentId()).isNull();
        }

        @Test
        @DisplayName("should set parentDeleted when parent is inactive")
        void mapToAdminResponse_deletedParent_setsParentDeleted() {
            // Arrange
            Post parentPost = createTestPost(UUID.randomUUID(), "parent", "Parent post", testUser);
            parentPost.setActive(false); // Parent is deleted
            Post comment = createTestPost(UUID.randomUUID(), "reply", "Comment content", testUser);
            setField(comment, "parent", parentPost);

            when(postRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(comment);

            // Act
            AdminPostResponse result = postService.adminToggleActive(comment.getId(), true);

            // Assert
            assertThat(result.parentDeleted()).isTrue();
        }

        @Test
        @DisplayName("should prepend # to subject")
        void mapToAdminResponse_prependsHashtag() {
            // Arrange
            when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
            setupAdminMocksForSingle(testPost);

            // Act
            AdminPostResponse result = postService.adminToggleActive(testPostId, true);

            // Assert
            assertThat(result.subject()).startsWith("#");
        }
    }

    @Nested
    @DisplayName("createPost() - comment edge cases")
    class CreatePostCommentEdgeCaseTests {

        @Test
        @DisplayName("should throw exception when creating comment on deleted parent")
        void createPost_deletedParent_throwsException() {
            // Arrange
            Post deletedParent = createTestPost(UUID.randomUUID(), "deleted", "Deleted post", testUser);
            deletedParent.setActive(false);

            PostCreateRequest request = new PostCreateRequest("reply", "Comment on deleted post");
            request.setParentId(deletedParent.getId());

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.findById(deletedParent.getId())).thenReturn(Optional.of(deletedParent));

            // Act & Assert
            assertThatThrownBy(() -> postService.createPost(request, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot comment on a deleted post");

            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when creating nested comment (comment on comment)")
        void createPost_nestedComment_throwsException() {
            // Arrange
            Post parentPost = createTestPost(UUID.randomUUID(), "original", "Original post", testUser);
            Post existingComment = createTestPost(UUID.randomUUID(), "comment", "First comment", testUser);
            setField(existingComment, "parent", parentPost); // existingComment is already a comment

            PostCreateRequest request = new PostCreateRequest("reply", "Nested comment");
            request.setParentId(existingComment.getId());

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.findById(existingComment.getId())).thenReturn(Optional.of(existingComment));

            // Act & Assert
            assertThatThrownBy(() -> postService.createPost(request, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot reply to a comment");

            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when parent post not found")
        void createPost_parentNotFound_throwsException() {
            // Arrange
            UUID nonExistentParentId = UUID.randomUUID();
            PostCreateRequest request = new PostCreateRequest("reply", "Comment");
            request.setParentId(nonExistentParentId);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(postRepository.findById(nonExistentParentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.createPost(request, testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Parent post not found");

            verify(postRepository, never()).save(any());
        }
    }
}
