package at.fhtw.webenprjbackend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.fhtw.webenprjbackend.dto.BookmarkCollectionResponse;
import at.fhtw.webenprjbackend.dto.BookmarkCreateResult;
import at.fhtw.webenprjbackend.dto.BookmarkRequest;
import at.fhtw.webenprjbackend.dto.BookmarkResponse;
import at.fhtw.webenprjbackend.dto.CollectionCreateRequest;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.BookmarkService;
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

/**
 * REST Controller for managing bookmarks and collections.
 * Provides idempotent operations for bookmarking posts and organizing them into collections.
 */
@RestController
@RequestMapping("/bookmarks")
@Tag(name = "Bookmarks", description = "Bookmark posts and organize them into collections")
@Validated
public class BookmarkController {

    private static final String MEDIA_TYPE_JSON = "application/json";

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }


    @PostMapping("/posts/{postId}")
    @Operation(
        summary = "Create a bookmark",
        description = "Bookmark a post with optional collection and notes. Idempotent - returns existing bookmark if already bookmarked.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Bookmark created",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = BookmarkResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Bookmark already exists (idempotent)",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = BookmarkResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Post not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<BookmarkResponse> createBookmark(
            @Parameter(description = "Post UUID to bookmark", required = true)
            @PathVariable UUID postId,
            @Valid @RequestBody(required = false) BookmarkRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        BookmarkRequest actualRequest = request != null ? request : new BookmarkRequest(null, null);
        BookmarkCreateResult result = bookmarkService.createBookmark(postId, principal.getId(), actualRequest);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.bookmark());
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(
        summary = "Remove a bookmark",
        description = "Remove a bookmark from a post. Idempotent - succeeds even if bookmark doesn't exist.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bookmark removed (idempotent)"),
        @ApiResponse(responseCode = "404", description = "Post not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Void> deleteBookmark(
            @Parameter(description = "Post UUID to unbookmark", required = true)
            @PathVariable UUID postId,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        bookmarkService.deleteBookmark(postId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/posts/{postId}")
    @Operation(
        summary = "Update bookmark metadata",
        description = "Update bookmark's collection or notes. Can be used to move bookmark between collections.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bookmark updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = BookmarkResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Bookmark or post not found"),
        @ApiResponse(responseCode = "403", description = "Cannot add to another user's collection"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<BookmarkResponse> updateBookmark(
            @Parameter(description = "Post UUID", required = true)
            @PathVariable UUID postId,
            @Valid @RequestBody BookmarkRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        BookmarkResponse updated = bookmarkService.updateBookmark(postId, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(
        summary = "Get all user bookmarks",
        description = "Retrieve all bookmarks for the authenticated user, paginated and ordered by creation date (newest first).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bookmarks retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = BookmarkResponse.class))
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<BookmarkResponse>> getUserBookmarks(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookmarkResponse> bookmarks = bookmarkService.getUserBookmarks(principal.getId(), pageable);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/uncategorized")
    @Operation(
        summary = "Get uncategorized bookmarks",
        description = "Retrieve bookmarks not assigned to any collection, paginated and ordered by creation date (newest first).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Uncategorized bookmarks retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = BookmarkResponse.class))
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<BookmarkResponse>> getUncategorizedBookmarks(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookmarkResponse> bookmarks = bookmarkService.getUncategorizedBookmarks(principal.getId(), pageable);
        return ResponseEntity.ok(bookmarks);
    }


    @PostMapping("/collections")
    @Operation(
        summary = "Create a collection",
        description = "Create a new bookmark collection (folder) with optional color and icon for visual distinction.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Collection created successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = BookmarkCollectionResponse.class)
            )
        ),
        @ApiResponse(responseCode = "409", description = "Collection with this name already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid collection data"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<BookmarkCollectionResponse> createCollection(
            @Valid @RequestBody CollectionCreateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        BookmarkCollectionResponse collection = bookmarkService.createCollection(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(collection);
    }

    @GetMapping("/collections")
    @Operation(
        summary = "Get all user collections",
        description = "Retrieve all bookmark collections for the authenticated user, ordered by creation date.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Collections retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = BookmarkCollectionResponse.class))
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<BookmarkCollectionResponse>> getUserCollections(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        List<BookmarkCollectionResponse> collections = bookmarkService.getUserCollections(principal.getId());
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/collections/{id}")
    @Operation(
        summary = "Get bookmarks in a collection",
        description = "Retrieve all bookmarks within a specific collection, paginated and ordered by creation date (newest first).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Collection bookmarks retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = BookmarkResponse.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "Collection not found"),
        @ApiResponse(responseCode = "403", description = "Cannot access another user's collection"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<BookmarkResponse>> getCollectionBookmarks(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookmarkResponse> bookmarks = bookmarkService.getCollectionBookmarks(id, principal.getId(), pageable);
        return ResponseEntity.ok(bookmarks);
    }

    @PutMapping("/collections/{id}")
    @Operation(
        summary = "Update a collection",
        description = "Update collection metadata including name, description, color, and icon.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Collection updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = BookmarkCollectionResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Collection not found"),
        @ApiResponse(responseCode = "403", description = "Cannot update another user's collection"),
        @ApiResponse(responseCode = "409", description = "Collection with this name already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid collection data"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<BookmarkCollectionResponse> updateCollection(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody CollectionCreateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        BookmarkCollectionResponse updated = bookmarkService.updateCollection(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/collections/{id}")
    @Operation(
        summary = "Delete a collection",
        description = "Delete a collection. Bookmarks in this collection will become uncategorized (collection_id set to NULL).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Collection deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Collection not found"),
        @ApiResponse(responseCode = "403", description = "Cannot delete another user's collection"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Void> deleteCollection(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        bookmarkService.deleteCollection(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
