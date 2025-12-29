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
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final BookmarkService bookmarkService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostLikeRepository postLikeRepository, FollowRepository followRepository,
                       BookmarkService bookmarkService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.followRepository = followRepository;
        this.bookmarkService = bookmarkService;
    }

    public Page<PostResponse> getAllPosts(Pageable pageable, UUID currentUserId) {
        // Only return active top-level posts (not comments)
        Page<Post> posts = postRepository.findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(pageable);
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
        // Only return active top-level posts (not comments)
        Page<Post> posts = postRepository.findByParentIsNullAndActiveTrueAndUserIdInOrderByCreatedAtDesc(follows, pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    public PostResponse getPostById(UUID id, UUID currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        // Allow fetching inactive posts (to show "original post was deleted" message)
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

        // Handle parent post for comments
        if (request.getParentId() != null) {
            Post parent = postRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent post not found"));
            if (!parent.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot comment on a deleted post");
            }
            post.setParent(parent);
        }

        Post saved = postRepository.save(post);
        return mapSingleWithLikes(saved, userId);
    }

    /**
     * Get comments for a post (paginated).
     */
    public Page<PostResponse> getCommentsForPost(UUID postId, Pageable pageable, UUID currentUserId) {
        // Verify parent exists
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        Page<Post> comments = postRepository.findByParentIdAndActiveTrueOrderByCreatedAtAsc(postId, pageable);
        return mapPageWithLikes(comments, currentUserId);
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
     * Soft-deletes a post by setting its active flag to false.
     * The post remains in the database so child comments can display
     * "original post was deleted" message.
     *
     * @param id the UUID of the post to delete
     * @throws ResponseStatusException with NOT_FOUND status if post doesn't exist
     */
    @Transactional
    public void deletePost(UUID id) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        existing.setActive(false);
        postRepository.save(existing);
    }

    public Page<PostResponse> searchPosts(String keyword, Pageable pageable, UUID currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts(pageable, currentUserId);
        }
        // Only search active top-level posts (not comments)
        Page<Post> posts = postRepository.findByParentIsNullAndActiveTrueAndContentContainingIgnoreCase(
                keyword.trim(), pageable);
        return mapPageWithLikes(posts, currentUserId);
    }

    public long getPostCount() {
        return postRepository.count();
    }

    public Page<PostResponse> searchBySubject(String subject, Pageable pageable, UUID currentUserId) {
        String normalized = normalizeSubject(subject);
        // Only search active top-level posts (not comments)
        Page<Post> posts = postRepository.findByParentIsNullAndActiveTrueAndSubjectIgnoreCase(normalized, pageable);
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
    private PostResponse mapToResponse(Post post, Map<UUID, Long> likeCounts, Set<UUID> likedByCurrentUser,
                                        Map<UUID, Long> bookmarkCounts, Set<UUID> bookmarkedByCurrentUser,
                                        Map<UUID, Long> commentCounts) {
        long likeCount = likeCounts.getOrDefault(post.getId(), 0L);
        boolean isLiked = likedByCurrentUser.contains(post.getId());
        long bookmarkCount = bookmarkCounts.getOrDefault(post.getId(), 0L);
        boolean isBookmarked = bookmarkedByCurrentUser.contains(post.getId());
        long commentCount = commentCounts.getOrDefault(post.getId(), 0L);

        // Check if parent post is deleted
        boolean parentDeleted = post.getParent() != null && !post.getParent().isActive();

        return new PostResponse(
                post.getId(),
                post.getParent() != null ? post.getParent().getId() : null,
                commentCount,
                parentDeleted,
                "#" + post.getSubject(), // Add '#' for frontend display
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getProfileImageUrl(),
                likeCount,
                isLiked,
                bookmarkCount,
                isBookmarked
        );
    }

    private Page<PostResponse> mapPageWithLikes(Page<Post> posts, UUID currentUserId) {
        Map<UUID, Long> likeCounts = fetchLikeCounts(posts.getContent());
        Set<UUID> likedByCurrentUser = fetchLikedPostIds(posts.getContent(), currentUserId);
        Map<UUID, Long> bookmarkCounts = bookmarkService.fetchBookmarkCounts(posts.getContent());
        Set<UUID> bookmarkedByCurrentUser = bookmarkService.fetchBookmarkedPostIds(posts.getContent(), currentUserId);
        Map<UUID, Long> commentCounts = fetchCommentCounts(posts.getContent());
        return posts.map(post -> mapToResponse(post, likeCounts, likedByCurrentUser, bookmarkCounts, bookmarkedByCurrentUser, commentCounts));
    }

    private PostResponse mapSingleWithLikes(Post post, UUID currentUserId) {
        Map<UUID, Long> likeCounts = fetchLikeCounts(List.of(post));
        Set<UUID> likedByCurrentUser = fetchLikedPostIds(List.of(post), currentUserId);
        Map<UUID, Long> bookmarkCounts = bookmarkService.fetchBookmarkCounts(List.of(post));
        Set<UUID> bookmarkedByCurrentUser = bookmarkService.fetchBookmarkedPostIds(List.of(post), currentUserId);
        Map<UUID, Long> commentCounts = fetchCommentCounts(List.of(post));
        return mapToResponse(post, likeCounts, likedByCurrentUser, bookmarkCounts, bookmarkedByCurrentUser, commentCounts);
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

    /**
     * Batch fetch comment counts for a list of posts to prevent N+1 queries.
     */
    private Map<UUID, Long> fetchCommentCounts(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return postRepository.countCommentsByParentIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
}
