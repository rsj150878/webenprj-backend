package at.fhtw.webenprjbackend.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.controller.PostController;
import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.PostLikeRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * Service layer for managing study posts in the Motivise platform.
 *
 * @see Post
 * @see PostRepository
 * @see PostController
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostLikeRepository postLikeRepository, FollowRepository followRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.followRepository = followRepository;
    }

    public Page<PostResponse> getAllPosts(Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    public Page<PostResponse> getFollowingPosts(Pageable pageable, UUID currentUserId) {
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required for following feed");
        }
        User current = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var follows = followRepository.findByFollower(current, Pageable.unpaged()).stream()
                .map(f -> f.getFollowed().getId())
                .toList();
        if (follows.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(follows, pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    public PostResponse getPostById(UUID id, UUID currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return mapSingleWithLikes(post, currentUserId);
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest request, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String normalizedSubject = normalizeSubject(request.getSubject());

        Post post = new Post(
                normalizedSubject,
                request.getContent(),
                request.getImageUrl(),
                user
        );

        Post saved = postRepository.save(post);
        return mapSingleWithLikes(saved, userId);
    }

    @Transactional
    public PostResponse updatePost(UUID id, PostUpdateRequest request) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.getSubject() != null) {
            existing.setSubject(normalizeSubject(request.getSubject()));
        }

        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            existing.setImageUrl(request.getImageUrl());
        }

        Post saved = postRepository.save(existing);
        return mapSingleWithLikes(saved, null);
    }

    /**
     * Deletes a post by its ID.
     * @param id the UUID of the post to delete
     * @throws ResponseStatusException with NOT_FOUND status if post doesn't exist
     */
    @Transactional
    public void deletePost(UUID id) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        postRepository.delete(existing);
    }

    public Page<PostResponse> searchPosts(String keyword, Pageable pageable, UUID currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts(pageable, currentUserId);
        }
        Page<Post> posts = postRepository.findByContentContainingIgnoreCase(keyword.trim(), pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    public long getPostCount() {
        return postRepository.count();
    }

    public Page<PostResponse> searchBySubject(String subject, Pageable pageable, UUID currentUserId) {
        String normalized = normalizeSubject(subject);
        Page<Post> posts = postRepository.findBySubjectIgnoreCase(normalized, pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    /**
     * Normalizes subject by removing leading '#' if present.
     *
     * @param subject the subject to normalize (may or may not start with '#')
     * @return normalized subject without leading '#', or null if input is null
     */
    private String normalizeSubject(String subject) {
        if (subject == null) {
            return null;
        }
        return subject.startsWith("#") ? subject.substring(1) : subject;
    }

    /**
     * Converts a Post entity to a PostResponse DTO for API responses.
     *
     * @param post the Post entity to convert
     * @return PostResponse DTO with '#' prepended to subject
     */
    private PostResponse mapToResponse(Post post, Map<UUID, Long> likeCounts, Set<UUID> likedByCurrentUser) {
        long likeCount = likeCounts.getOrDefault(post.getId(), 0L);
        boolean isLiked = likedByCurrentUser.contains(post.getId());
        return new PostResponse(
                post.getId(),
                "#" + post.getSubject(), // Add '#' for frontend display
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getProfileImageUrl(),
                likeCount,
                isLiked
        );
    }

    private Page<PostResponse> mapPageWithLikes(Page<Post> posts, UUID currentUserId) {
        Map<UUID, Long> likeCounts = fetchLikeCounts(posts.getContent());
        Set<UUID> likedByCurrentUser = fetchLikedPostIds(posts.getContent(), currentUserId);
        return posts.map(post -> mapToResponse(post, likeCounts, likedByCurrentUser));
    }

    private PostResponse mapSingleWithLikes(Post post, UUID currentUserId) {
        Map<UUID, Long> likeCounts = fetchLikeCounts(List.of(post));
        Set<UUID> likedByCurrentUser = fetchLikedPostIds(List.of(post), currentUserId);
        return mapToResponse(post, likeCounts, likedByCurrentUser);
    }

    private Map<UUID, Long> fetchLikeCounts(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return postLikeRepository.countLikesByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    private Set<UUID> fetchLikedPostIds(List<Post> posts, UUID currentUserId) {
        if (currentUserId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return Set.copyOf(postLikeRepository.findLikedPostIds(currentUserId, postIds));
    }
}
