package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for a bookmark with full post details and collection information.
 * Used when returning individual bookmarks or lists of bookmarks.
 */
@Schema(description = "Bookmark with complete post and collection details")
public record BookmarkResponse(
    @Schema(description = "Bookmark UUID",
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Complete post details")
    PostResponse post,

    @Schema(description = "Collection details (null if uncategorized)",
            nullable = true)
    BookmarkCollectionResponse collection,

    @Schema(description = "Personal notes about this bookmark",
            example = "Great resource for exam prep!",
            nullable = true)
    String notes,

    @Schema(description = "Bookmark creation timestamp")
    LocalDateTime createdAt
) {}
