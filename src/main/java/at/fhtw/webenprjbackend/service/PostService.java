// TODO: Probably have to move a bunch of this to the Repository layer
package at.fhtw.webenprjbackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.repository.PostRepository;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service layer for post operations.
 * Contains business logic and connects the controller with the repository.
 *
 * Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.2
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Constructor injection (preferred over @Autowired on fields)
     */
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all posts ordered by creation date (newest first)
     * Used to display the main feed.
     */
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            responses.add(mapToResponse(post));
        }
        return responses;
    }

    /**
     * Get a single post by its UUID.
     * Throws 404 if not found.
     */
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return mapToResponse(post);
    }

    /**
     * Create a new post.
     * For Milestone 1: userId comes from request.
     * Later: from JWT.
     */
    public PostResponse createPost(PostCreateRequest request) {

        if (request.getUserId

                () == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Normalize subject (remove leading # before saving)
        String normalizedSubject = request.getSubject().startsWith("#")
                ? request.getSubject().substring(1)
                : request.getSubject();

        Post post = new Post(
                normalizedSubject,
                request.getContent(),
                request.getImageUrl(),
                user
        );

        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    /**
     * Update an existing post.
     * Only provided (non-null) fields will be changed.
     */
    public PostResponse updatePost(UUID id, PostUpdateRequest request) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.getSubject() != null) {
            String normalizedSubject = request.getSubject().startsWith("#")
                    ? request.getSubject().substring(1)
                    : request.getSubject();
            existing.setSubject(normalizedSubject);
        }

        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            existing.setImageUrl(request.getImageUrl());
        }

        Post saved = postRepository.save(existing);
        return mapToResponse(saved);
    }

    /**
     * Delete a post by UUID.
     * Throws 404 if post does not exist.
     */
    public void deletePost(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        postRepository.deleteById(id);
    }

    /**
     * Search posts by keyword (in content).
     * Returns all posts if keyword is empty.
     */
    public List<PostResponse> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts();
        }
        List<Post> posts =
                postRepository.findByContentContainingIgnoreCase(keyword.trim());
        List<PostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            responses.add(mapToResponse(post));
        }
        return responses;
    }

    /** Count all existing posts (for dashboard or statistics). */
    public long getPostCount() {
        return postRepository.count();
    }

    /**
     * Search posts by exact subject (ignores case, normalizes leading '#').
     */
    public List<PostResponse> searchBySubject(String subject) {
        String normalized = subject.startsWith("#")
                ? subject.substring(1)
                : subject;
        List<Post> posts = postRepository.findBySubjectIgnoreCase(normalized);
        return posts.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Helper method to convert a Post entity into a PostResponse DTO.
     * Adds '#' to the subject for frontend display.
     */
    private PostResponse mapToResponse(Post post) {
        return new PostResponse(
                post.getId(),
                "#" + post.getSubject(), // for Frontend pretty with #
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(),
                post.getUser().getUsername()
        );
    }
}
