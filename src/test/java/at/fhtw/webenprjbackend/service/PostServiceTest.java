package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostService using JUnit 5 and Mockito
 * Tests business logic, repository interactions, and error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private static User testUser;
    private static Post testPost;
    private static UUID userId;
    private static UUID postId;

    @BeforeAll
    static void setUpTestData() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        postId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    }

    @BeforeEach
    void setUp() {
        // Create fresh test data for each test
        testUser = new User(
            "test@example.com", 
            "testuser", 
            "password123", 
            "AT", 
            "https://example.com/avatar.png", 
            Role.USER
        );

        testPost = new Post("JavaLearning", "Learning Spring Boot with dependency injection!", 
                           "https://example.com/spring.png", testUser);
    }

    @Nested
    @DisplayName("Get All Posts")
    class GetAllPostsTests {

        @Test
        @DisplayName("Should return all posts ordered by creation date")
        void getAllPosts_WhenPostsExist_ShouldReturnOrderedList() {
            // Given
            Post post1 = new Post("Java", "Content 1", null, testUser);
            Post post2 = new Post("Spring", "Content 2", null, testUser);
            List<Post> mockPosts = Arrays.asList(post1, post2);
            
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockPosts);

            // When
            List<PostResponse> responses = postService.getAllPosts();

            // Then
            assertEquals(2, responses.size());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("Should return empty list when no posts exist")
        void getAllPosts_WhenNoPostsExist_ShouldReturnEmptyList() {
            // Given
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList());

            // When
            List<PostResponse> responses = postService.getAllPosts();

            // Then
            assertTrue(responses.isEmpty());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
        }
    }

    @Nested
    @DisplayName("Get Post By ID")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should return post when valid ID provided")
        void getPostById_WhenValidId_ShouldReturnPost() {
            // Given
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));

            // When
            PostResponse response = postService.getPostById(postId);

            // Then
            assertNotNull(response);
            assertEquals("#JavaLearning", response.getSubject());
            assertEquals("Learning Spring Boot with dependency injection!", response.getContent());
            verify(postRepository).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Should throw exception when post not found")
        void getPostById_WhenPostNotFound_ShouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.getPostById(nonExistentId)
            );
            
            assertEquals("Post not found", exception.getReason());
            verify(postRepository).findById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Create Post")
    class CreatePostTests {

        @Test
        @DisplayName("Should create post successfully")
        void createPost_WhenValidRequest_ShouldCreatePost() {
            // Given
            PostCreateRequest request = new PostCreateRequest("#JavaLearning", "New learning content", "https://example.com/image.png");
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            // When
            PostResponse response = postService.createPost(request, userId);

            // Then
            assertNotNull(response);
            assertEquals("#JavaLearning", response.getSubject());
            verify(userRepository).findById(any(UUID.class));
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Should normalize subject when creating post")
        void createPost_WhenSubjectHasHashtag_ShouldNormalizeSubject() {
            // Given
            PostCreateRequest request = new PostCreateRequest("#SpringBoot", "Spring Boot content", null);
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post savedPost = invocation.getArgument(0);
                // Verify that the # is removed before saving
                assertEquals("SpringBoot", savedPost.getSubject());
                return testPost;
            });

            // When
            postService.createPost(request, userId);

            // Then
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createPost_WhenUserNotFound_ShouldThrowException() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();
            PostCreateRequest request = new PostCreateRequest("#Test", "Content", null);
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.createPost(request, nonExistentUserId)
            );
            
            assertEquals("User not found", exception.getReason());
            verify(userRepository).findById(any(UUID.class));
            verify(postRepository, never()).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Update Post")
    class UpdatePostTests {

        @Test
        @DisplayName("Should update post when user is owner")
        void updatePost_WhenUserIsOwner_ShouldUpdatePost() {
            // Given
            PostUpdateRequest request = new PostUpdateRequest("UpdatedSubject", "Updated content", "https://example.com/new.png");
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            // When
            PostResponse response = postService.updatePost(postId, request, userId, false);

            // Then
            assertNotNull(response);
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Should allow admin to update any post")
        void updatePost_WhenUserIsAdmin_ShouldUpdatePost() {
            // Given
            UUID adminUserId = UUID.randomUUID();
            PostUpdateRequest request = new PostUpdateRequest("AdminUpdate", "Admin updated content", null);
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            // When
            PostResponse response = postService.updatePost(postId, request, adminUserId, true);

            // Then
            assertNotNull(response);
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to update")
        void updatePost_WhenUserIsNotOwnerOrAdmin_ShouldThrowException() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            PostUpdateRequest request = new PostUpdateRequest("HackerUpdate", "Hacker content", null);
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));

            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.updatePost(postId, request, otherUserId, false)
            );
            
            assertEquals("You are not allowed to edit this post", exception.getReason());
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository, never()).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Delete Post")
    class DeletePostTests {

        @Test
        @DisplayName("Should delete post when user is owner")
        void deletePost_WhenUserIsOwner_ShouldDeletePost() {
            // Given
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));

            // When
            postService.deletePost(postId, userId, false);

            // Then
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("Should allow admin to delete any post")
        void deletePost_WhenUserIsAdmin_ShouldDeletePost() {
            // Given
            UUID adminUserId = UUID.randomUUID();
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));

            // When
            postService.deletePost(postId, adminUserId, true);

            // Then
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to delete")
        void deletePost_WhenUserIsNotOwnerOrAdmin_ShouldThrowException() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPost));

            // When & Then
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.deletePost(postId, otherUserId, false)
            );
            
            assertEquals("You are not allowed to delete this post", exception.getReason());
            verify(postRepository).findById(any(UUID.class));
            verify(postRepository, never()).delete(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Search Posts")
    class SearchPostsTests {

        @Test
        @DisplayName("Should search posts by keyword")
        void searchPosts_WhenKeywordProvided_ShouldReturnMatchingPosts() {
            // Given
            String keyword = "Spring";
            List<Post> mockPosts = Arrays.asList(testPost);
            when(postRepository.findByContentContainingIgnoreCase(keyword)).thenReturn(mockPosts);

            // When
            List<PostResponse> responses = postService.searchPosts(keyword);

            // Then
            assertEquals(1, responses.size());
            verify(postRepository).findByContentContainingIgnoreCase(keyword);
        }

        @Test
        @DisplayName("Should return all posts when search keyword is empty")
        void searchPosts_WhenKeywordIsEmpty_ShouldReturnAllPosts() {
            // Given
            List<Post> allPosts = Arrays.asList(testPost);
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(allPosts);

            // When
            List<PostResponse> responses = postService.searchPosts("");

            // Then
            assertEquals(1, responses.size());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
            verify(postRepository, never()).findByContentContainingIgnoreCase(any());
        }

        @Test
        @DisplayName("Should return all posts when search keyword is null")
        void searchPosts_WhenKeywordIsNull_ShouldReturnAllPosts() {
            // Given
            List<Post> allPosts = Arrays.asList(testPost);
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(allPosts);

            // When
            List<PostResponse> responses = postService.searchPosts(null);

            // Then
            assertEquals(1, responses.size());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
            verify(postRepository, never()).findByContentContainingIgnoreCase(any());
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should return post count")
        void getPostCount_ShouldReturnRepositoryCount() {
            // Given
            when(postRepository.count()).thenReturn(5L);

            // When
            long count = postService.getPostCount();

            // Then
            assertEquals(5L, count);
            verify(postRepository).count();
        }

        @Test
        @DisplayName("Should search posts by subject")
        void searchBySubject_WhenSubjectProvided_ShouldReturnMatchingPosts() {
            // Given
            String subject = "#JavaLearning";
            List<Post> mockPosts = Arrays.asList(testPost);
            when(postRepository.findBySubjectIgnoreCase("JavaLearning")).thenReturn(mockPosts);

            // When
            List<PostResponse> responses = postService.searchBySubject(subject);

            // Then
            assertEquals(1, responses.size());
            verify(postRepository).findBySubjectIgnoreCase("JavaLearning");
        }
    }
}