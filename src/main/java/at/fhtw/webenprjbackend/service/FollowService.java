package at.fhtw.webenprjbackend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.Follow;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void follow(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            return; // idempotent
        }
        followRepository.save(new Follow(follower, followed));
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            return;
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        followRepository.deleteByFollowerAndFollowed(follower, followed);
    }

    public Page<UserResponse> getFollowers(UUID userId, Pageable pageable) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return followRepository.findByFollowed(target, pageable)
                .map(follow -> toResponse(follow.getFollower()));
    }

    public Page<UserResponse> getFollowing(UUID userId, Pageable pageable) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return followRepository.findByFollower(target, pageable)
                .map(follow -> toResponse(follow.getFollowed()));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                followerCount(user),
                followingCount(user)
        );
    }

    public long followerCount(User user) {
        return followRepository.countByFollowed(user);
    }

    public long followingCount(User user) {
        return followRepository.countByFollower(user);
    }
}
