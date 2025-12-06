package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Data Transfer Object for Media entity responses.
 * Contains file metadata without sensitive internal storage details.
 */
@Schema(description = "Media file information")
public record MediaDto(
    @Schema(description = "Unique media identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "MIME type of the file", example = "image/jpeg")
    String contentType,

    @Schema(description = "Original filename", example = "study-diagram.jpg")
    String name
) {
    // Note: externalId is intentionally excluded - it's an internal storage detail
}
