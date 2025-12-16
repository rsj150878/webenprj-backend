package at.fhtw.webenprjbackend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating bookmark metadata.
 * Users can move bookmarks between collections or update personal notes.
 */
@Schema(description = "Request to update bookmark metadata")
public record BookmarkUpdateRequest(
    @Schema(description = "Collection UUID (null to uncategorize the bookmark)",
            example = "123e4567-e89b-12d3-a456-426614174000",
            nullable = true)
    UUID collectionId,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Personal notes about this bookmark",
            example = "Updated notes")
    String notes
) {}
