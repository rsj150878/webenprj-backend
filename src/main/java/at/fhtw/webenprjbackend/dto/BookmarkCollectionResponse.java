package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for bookmark collection with metadata and computed fields.
 * Includes bookmark count for displaying in UI.
 */
@Schema(description = "Bookmark collection with metadata and bookmark count")
public record BookmarkCollectionResponse(
    @Schema(description = "Collection UUID",
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Collection name",
            example = "Math Finals")
    String name,

    @Schema(description = "Collection description",
            example = "Resources for preparing for my math final exam",
            nullable = true)
    String description,

    @Schema(description = "Hex color code",
            example = "#3B82F6",
            nullable = true)
    String color,

    @Schema(description = "Heroicon name",
            example = "AcademicCapIcon",
            nullable = true)
    String iconName,

    @Schema(description = "Number of bookmarks in this collection",
            example = "12")
    long bookmarkCount,

    @Schema(description = "Collection creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Collection last update timestamp")
    LocalDateTime updatedAt
) {}
