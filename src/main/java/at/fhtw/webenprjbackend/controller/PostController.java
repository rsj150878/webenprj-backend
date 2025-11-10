package at.fhtw.webenprjbackend.controller;

import java.util.List;
import java.util.UUID;
import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import at.fhtw.webenprjbackend.service.PostService;
import jakarta.validation.Valid;

/**
 * REST Controller for Post operations.
 * Handles HTTP requests for the study blogging platform.
 * 
 * @author Wii
 * @version 0.1
 *
 * @author jasmin
 * @version 0.2
 */
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * Constructor injection for PostService.
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Get all posts (main feed)
     *
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /**
     * Get a specific post by ID
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * Create a new post
     *
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request) {
        PostResponse created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing post
     *
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request) {
        PostResponse updated = postService.updatePost(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a post
     *
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search posts by keyword
     *
     */
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(postService.searchPosts(keyword));
    }

}
