package at.fhtw.webenprjbackend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a bookmark.
 * Users can optionally assign the bookmark to a collection and add personal notes.
 */
@Schema(description = "Request to create or update a bookmark")
public record BookmarkRequest(
    @Schema(description = "Collection UUID (optional - null to leave uncategorized)",
            example = "123e4567-e89b-12d3-a456-426614174000",
            nullable = true)
    UUID collectionId,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Personal notes about this bookmark (optional)",
            example = "Great resource for exam prep!")
    String notes
) {}
