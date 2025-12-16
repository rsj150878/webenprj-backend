package at.fhtw.webenprjbackend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a bookmark.
 * Users can optionally assign the bookmark to a collection and add personal notes.
 */
@Schema(description = "Request to create a bookmark")
public record BookmarkCreateRequest(
    @Schema(description = "Collection UUID (optional - bookmark will be uncategorized if not provided)",
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID collectionId,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Personal notes about this bookmark (optional)",
            example = "Great resource for exam prep!")
    String notes
) {}
