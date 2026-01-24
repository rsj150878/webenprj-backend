package at.fhtw.webenprjbackend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.fhtw.webenprjbackend.dto.AdminPostResponse;
import at.fhtw.webenprjbackend.dto.AdminPostStatsResponse;
import at.fhtw.webenprjbackend.dto.PostCreateRequest;
import at.fhtw.webenprjbackend.dto.PostResponse;
import at.fhtw.webenprjbackend.dto.PostUpdateRequest;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;

/**
 * Post management endpoints.
 */
@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Study posts management - create, read, update, delete and search study updates")
@Validated
public class PostController {

    private static final String MEDIA_TYPE_JSON = "application/json";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(
        summary = "Get all posts or search posts",
        description = "Retrieve all study posts ordered by creation date (newest first), or search posts by keyword. " +
                      "Use the 'search' query parameter to filter results. RESTful design: filtering via query parameters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully (all posts or search results)",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
            )
        )
    })
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @Parameter(
                description = "Optional search keyword to filter posts by content (case-insensitive)",
                example = "java",
                required = false
            )
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size,
            @RequestParam(required = false, defaultValue = "all")
            @Pattern(regexp = "all|following", flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "filter must be 'all' or 'following'")
            String filter,
            @Parameter(
                description = "Optional subject/tag to filter posts by (case-insensitive)",
                example = "programming",
                required = false
            )
            @RequestParam(required = false) String subject,
            @Parameter(
                description = "Optional author ID to filter posts by a specific user",
                example = "123e4567-e89b-12d3-a456-426614174000",
                required = false
            )
            @RequestParam(required = false) UUID authorId,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID currentUserId = extractUserId(authentication);

        // RESTful approach: use query parameter to filter collection
        if ("following".equalsIgnoreCase(filter)) {
            return ResponseEntity.ok(postService.getFollowingPosts(pageable, currentUserId));
        }
        if (authorId != null) {
            return ResponseEntity.ok(postService.getPostsByAuthor(authorId, pageable, currentUserId));
        }
        if (subject != null && !subject.isBlank()) {
            return ResponseEntity.ok(postService.searchBySubject(subject, pageable, currentUserId));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(postService.searchPosts(search, pageable, currentUserId));
        }
        return ResponseEntity.ok(postService.getAllPosts(pageable, currentUserId));
    }

    @GetMapping("/subjects")
    @Operation(
        summary = "Get available subjects/tags",
        description = "Retrieve all unique subjects/tags used in posts. Useful for filtering."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = String.class))
            )
        )
    })
    public ResponseEntity<java.util.List<String>> getSubjects() {
        return ResponseEntity.ok(postService.getAvailableSubjects());
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
            description = "Post not found"
        )
    })
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "Post UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            Authentication authentication) {
        UUID currentUserId = extractUserId(authentication);
        return ResponseEntity.ok(postService.getPostById(id, currentUserId));
    }

    @GetMapping("/{id}/comments")
    @Operation(
        summary = "Get comments for a post",
        description = "Retrieve all direct comments on a post, paginated and ordered by creation time ascending."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found"
        )
    })
    public ResponseEntity<Page<PostResponse>> getComments(
            @Parameter(description = "Post UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        UUID currentUserId = extractUserId(authentication);
        return ResponseEntity.ok(postService.getCommentsForPost(id, pageable, currentUserId));
    }

    // POST Operations (Create)

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
            description = "Invalid post data"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required"
        )
    })
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request, 
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PostResponse created = postService.createPost(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT Operations (Update)

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
            description = "Invalid update data"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Not authorized to update this post"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Post not found"
        )
    })
    @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Post', 'update')")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "Post UUID to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request) {

        PostResponse updated = postService.updatePost(id, request);
        return ResponseEntity.ok(updated);
    }

    // DELETE Operations

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
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to delete this post"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Post not found"
        )
    })
    @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Post', 'delete')")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "Post UUID to delete", required = true)
            @PathVariable UUID id) {

        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/admin")
    @Operation(
        summary = "Get all posts for admin",
        description = "Retrieve all posts including inactive ones for admin moderation. Supports filtering by status and type.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = AdminPostResponse.class))
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminPostResponse>> adminGetAllPosts(
            @Parameter(description = "Filter by active status (null = all)")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by type: true = comments only, false = posts only, null = all")
            @RequestParam(required = false) Boolean isComment,
            @Parameter(description = "Search keyword in content or subject")
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(postService.adminGetAllPosts(active, isComment, search, pageable));
    }

    @GetMapping("/admin/stats")
    @Operation(
        summary = "Get post statistics",
        description = "Retrieve statistics about posts and comments for admin dashboard.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = AdminPostStatsResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostStatsResponse> adminGetStats() {
        return ResponseEntity.ok(postService.getAdminPostStats());
    }

    @PatchMapping("/admin/{id}/active")
    @Operation(
        summary = "Toggle post active status",
        description = "Soft delete or restore a post by setting its active status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post status updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = AdminPostResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin role required"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostResponse> adminToggleActive(
            @Parameter(description = "Post UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "New active status", required = true)
            @RequestParam boolean active) {

        return ResponseEntity.ok(postService.adminToggleActive(id, active));
    }

    @DeleteMapping("/admin/{id}")
    @Operation(
        summary = "Permanently delete post",
        description = "Hard delete a post. This action cannot be undone.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Post permanently deleted"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin role required"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminHardDelete(
            @Parameter(description = "Post UUID to permanently delete", required = true)
            @PathVariable UUID id) {

        postService.adminHardDeletePost(id);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null;
    }
}
