package at.fhtw.webenprjbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.repository.PostRepository;

/**
 * Service layer for Post operations.
 * Handles business logic and acts as a bridge between controllers and repositories.
 * 
 * @author Wii
 * @version 0.1
 */
@Service
public class PostService {

    private final PostRepository postRepository;

    /**
     * Constructor injection (preferred over @Autowired on fields)
     */
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Get all posts ordered by creation date (newest first).
     */
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get a specific post by ID.
     */
    public Optional<Post> getPostById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return postRepository.findById(id);
    }

    /**
     * Create a new study post.
     */
    public Post createPost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        return postRepository.save(post);
    }

    /**
     * Update an existing post.
     */
    public Optional<Post> updatePost(Long id, Post updatedPost) {
        if (id == null || updatedPost == null) {
            return Optional.empty();
        }
        
        return postRepository.findById(id)
            .map(existingPost -> {
                existingPost.setContent(updatedPost.getContent());
                existingPost.setTitle(updatedPost.getTitle());
                return postRepository.save(existingPost);
            });
    }

    /**
     * Delete a post by ID.
     */
    public boolean deletePost(Long id) {
        if (id == null) {
            return false;
        }
        
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Search posts by keyword in content (simplified version).
     */
    public List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts();
        }
        return postRepository.findByContentContainingIgnoreCase(keyword.trim());
    }

    /**
     * Get total number of posts.
     */
    public long getPostCount() {
        return postRepository.count();
    }
}
