package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostLike;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService")
class LikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private LikeService likeService;

    private User testUser;
    private Post testPost;
    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(postLikeRepository, postRepository, userRepository);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        testUser = createTestUser(userId, "testuser", "test@example.com");
        testPost = createTestPost(postId, "webdev", "Learning Spring Boot!", testUser);
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

    @Nested
    @DisplayName("like()")
    class LikeTests {

        @Test
        @DisplayName("should like post successfully")
        void like_success() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(postLikeRepository.existsByUserAndPost(testUser, testPost)).thenReturn(false);

            likeService.like(postId, userId);

            verify(postLikeRepository).save(any(PostLike.class));
        }

        @Test
        @DisplayName("should be idempotent when already liked")
        void like_alreadyLiked_idempotent() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(postLikeRepository.existsByUserAndPost(testUser, testPost)).thenReturn(true);

            likeService.like(postId, userId);

            verify(postLikeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void like_postNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.like(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void like_userNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.like(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("unlike()")
    class UnlikeTests {

        @Test
        @DisplayName("should unlike post successfully")
        void unlike_success() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            likeService.unlike(postId, userId);

            verify(postLikeRepository).deleteByUserAndPost(testUser, testPost);
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void unlike_postNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.unlike(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void unlike_userNotFound_throwsException() {
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.unlike(postId, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
