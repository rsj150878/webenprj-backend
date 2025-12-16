package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a bookmark collection.
 * Collections are user-owned folders for organizing bookmarks.
 */
@Schema(description = "Request to create or update a bookmark collection")
public record CollectionCreateRequest(
    @NotBlank(message = "Collection name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    @Schema(description = "Collection name",
            example = "Math Finals",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Schema(description = "Collection description (optional)",
            example = "Resources for preparing for my math final exam")
    String description,

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code (e.g., #3B82F6)")
    @Schema(description = "Hex color code for visual distinction (optional)",
            example = "#3B82F6")
    String color,

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    @Schema(description = "Heroicon name for visual distinction (optional)",
            example = "AcademicCapIcon")
    String iconName
) {}
