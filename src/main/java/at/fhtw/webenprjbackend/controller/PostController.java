package at.fhtw.webenprjbackend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for managing study posts.
 * Handles CRUD operations for posts including creation, reading, updating, and deletion.
 * Also provides search functionality for finding posts by keywords.
 */
@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Study posts management - create, read, update, delete and search study updates")
public class PostController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String MEDIA_TYPE_JSON = "application/json";

    private final PostService postService;

    /**
     * Constructor for dependency injection
     * @param postService Service layer for post operations
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Helper method to check if the authenticated user has admin role
     * @param authentication The authentication object
     * @return true if user is admin, false otherwise
     */
    private boolean isAdmin(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
    }

    // ===============================
    // GET Operations
    // ===============================

    @GetMapping
    @Operation(
        summary = "Get all posts",
        description = "Retrieve all study posts ordered by creation date (newest first). Public endpoint, no authentication required."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Posts retrieved successfully",
        content = @Content(
            mediaType = MEDIA_TYPE_JSON,
            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
        )
    )
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get post by ID",
        description = "Retrieve a specific post by its unique identifier. Public endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Post found and retrieved",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = PostResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Post not found",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Post Not Found",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Post not found with id: 123e4567-e89b-12d3-a456-426614174000\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        )
    })
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "Post UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search posts",
        description = "Search posts by keyword in content. Case-insensitive search across post content."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Search results retrieved (may be empty)",
        content = @Content(
            mediaType = MEDIA_TYPE_JSON,
            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
        )
    )
    public ResponseEntity<List<PostResponse>> searchPosts(
            @Parameter(description = "Search keyword to find in post content", required = true, example = "java") 
            @RequestParam("q") String keyword) {
        return ResponseEntity.ok(postService.searchPosts(keyword));
    }

    // ===============================
    // POST Operations (Create)
    // ===============================

    @PostMapping
    @Operation(
        summary = "Create new post",
        description = "Create a new study post. Requires authentication. The post will be associated with the authenticated user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Post created successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = PostResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid post data",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subject is required\",\"path\":\"/posts\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authentication Required",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"path\":\"/posts\"}"
                )
            )
        )
    })
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request, 
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PostResponse created = postService.createPost(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===============================
    // PUT Operations (Update)
    // ===============================

    @PutMapping("/{id}")
    @Operation(
        summary = "Update post",
        description = "Update an existing post. Users can only update their own posts unless they are admin. Admins can update any post.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Post updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = PostResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid update data",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Content cannot be empty\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authentication Required",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"path\":\"/posts/{id}\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Not authorized to update this post",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authorization Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only update your own posts\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Post not found",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Post Not Found",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Post not found with id: 123e4567-e89b-12d3-a456-426614174000\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        )
    })
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "Post UUID to update", required = true) 
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PostResponse updated = postService.updatePost(id, request, principal.getId(), isAdmin(authentication));
        return ResponseEntity.ok(updated);
    }

    // ===============================
    // DELETE Operations
    // ===============================

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete post",
        description = "Delete a post. Users can only delete their own posts unless they are admin. Admins can delete any post.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Post deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authentication Required",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"path\":\"/posts/{id}\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Not authorized to delete this post",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authorization Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only delete your own posts\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Post not found",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Post Not Found",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Post not found with id: 123e4567-e89b-12d3-a456-426614174000\",\"path\":\"/posts/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        )
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "Post UUID to delete", required = true) 
            @PathVariable UUID id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        postService.deletePost(id, principal.getId(), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }
}
