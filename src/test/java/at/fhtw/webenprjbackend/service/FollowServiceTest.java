package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.Follow;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService")
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    private FollowService followService;

    private User follower;
    private User followed;
    private UUID followerId;
    private UUID followedId;

    @BeforeEach
    void setUp() {
        followService = new FollowService(followRepository, userRepository);

        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();

        follower = createTestUser(followerId, "follower", "follower@example.com");
        followed = createTestUser(followedId, "followed", "followed@example.com");
    }

    private User createTestUser(UUID id, String username, String email) {
        User user = new User(email, username, "hashedPassword", "AT",
                "https://example.com/profile.png", "Dr.", Role.USER);
        setField(user, "id", id);
        setField(user, "createdAt", LocalDateTime.now());
        return user;
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
    @DisplayName("follow()")
    class FollowTests {

        @Test
        @DisplayName("should follow user successfully")
        void follow_success() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
            when(followRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(false);

            followService.follow(followerId, followedId);

            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("should be idempotent when already following")
        void follow_alreadyFollowing_idempotent() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
            when(followRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(true);

            followService.follow(followerId, followedId);

            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when trying to follow yourself")
        void follow_self_throwsException() {
            assertThatThrownBy(() -> followService.follow(followerId, followerId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot follow yourself");
        }

        @Test
        @DisplayName("should throw exception when follower not found")
        void follow_followerNotFound_throwsException() {
            when(userRepository.findById(followerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> followService.follow(followerId, followedId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Follower not found");
        }

        @Test
        @DisplayName("should throw exception when followed user not found")
        void follow_followedNotFound_throwsException() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> followService.follow(followerId, followedId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("unfollow()")
    class UnfollowTests {

        @Test
        @DisplayName("should unfollow user successfully")
        void unfollow_success() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));

            followService.unfollow(followerId, followedId);

            verify(followRepository).deleteByFollowerAndFollowed(follower, followed);
        }

        @Test
        @DisplayName("should do nothing when unfollowing yourself")
        void unfollow_self_doNothing() {
            followService.unfollow(followerId, followerId);

            verify(userRepository, never()).findById(any());
            verify(followRepository, never()).deleteByFollowerAndFollowed(any(), any());
        }

        @Test
        @DisplayName("should throw exception when follower not found")
        void unfollow_followerNotFound_throwsException() {
            when(userRepository.findById(followerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> followService.unfollow(followerId, followedId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Follower not found");
        }
    }

    @Nested
    @DisplayName("getFollowers()")
    class GetFollowersTests {

        @Test
        @DisplayName("should return paginated followers")
        void getFollowers_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Follow follow = new Follow(follower, followed);
            Page<Follow> followPage = new PageImpl<>(List.of(follow), pageable, 1);

            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
            when(followRepository.findByFollowed(followed, pageable)).thenReturn(followPage);
            when(followRepository.countByFollowed(follower)).thenReturn(0L);
            when(followRepository.countByFollower(follower)).thenReturn(0L);

            Page<UserResponse> result = followService.getFollowers(followedId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).username()).isEqualTo("follower");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getFollowers_userNotFound_throwsException() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findById(followedId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> followService.getFollowers(followedId, pageable))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getFollowing()")
    class GetFollowingTests {

        @Test
        @DisplayName("should return paginated following")
        void getFollowing_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Follow follow = new Follow(follower, followed);
            Page<Follow> followPage = new PageImpl<>(List.of(follow), pageable, 1);

            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(followRepository.findByFollower(follower, pageable)).thenReturn(followPage);
            when(followRepository.countByFollowed(followed)).thenReturn(0L);
            when(followRepository.countByFollower(followed)).thenReturn(0L);

            Page<UserResponse> result = followService.getFollowing(followerId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).username()).isEqualTo("followed");
        }
    }

    @Nested
    @DisplayName("followerCount() and followingCount()")
    class CountTests {

        @Test
        @DisplayName("should return follower count")
        void followerCount_success() {
            when(followRepository.countByFollowed(follower)).thenReturn(10L);

            long count = followService.followerCount(follower);

            assertThat(count).isEqualTo(10L);
        }

        @Test
        @DisplayName("should return following count")
        void followingCount_success() {
            when(followRepository.countByFollower(follower)).thenReturn(5L);

            long count = followService.followingCount(follower);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("isFollowing()")
    class IsFollowingTests {

        @Test
        @DisplayName("should return true when following")
        void isFollowing_true() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
            when(followRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(true);

            boolean result = followService.isFollowing(followerId, followedId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when not following")
        void isFollowing_false() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
            when(followRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(false);

            boolean result = followService.isFollowing(followerId, followedId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when follower not found")
        void isFollowing_followerNotFound_returnsFalse() {
            when(userRepository.findById(followerId)).thenReturn(Optional.empty());

            boolean result = followService.isFollowing(followerId, followedId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when followed not found")
        void isFollowing_followedNotFound_returnsFalse() {
            when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
            when(userRepository.findById(followedId)).thenReturn(Optional.empty());

            boolean result = followService.isFollowing(followerId, followedId);

            assertThat(result).isFalse();
        }
    }
}
