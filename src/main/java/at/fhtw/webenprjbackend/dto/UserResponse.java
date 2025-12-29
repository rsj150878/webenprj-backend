package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User information returned in responses")
public record UserResponse(
        @Schema(description = "Unique user identifier", example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "User's email address", example = "anna.schmidt@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Unique username", example = "study_anna",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "AT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String countryCode,

        @Schema(description = "Profile image URL (optional)", example = "https://example.com/images/profile1.png",
                nullable = true)
        String profileImageUrl,

        @Schema(description = "Salutation or title (optional)", example = "Dr.",
                nullable = true)
        String salutation,

        @Schema(description = "User role determining access permissions", example = "USER",
                allowableValues = {"USER", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String role,

        @Schema(description = "Account creation timestamp", example = "2024-11-27T10:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "Last profile update timestamp", example = "2024-11-27T15:45:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt,

        @Schema(description = "Number of followers", example = "42",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        long followerCount,

        @Schema(description = "Number of users this user follows", example = "10",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        long followingCount
) {
    /** Factory for when follower/following counts are not available. */
    public static UserResponse withoutCounts(UUID id, String email, String username, String countryCode,
                                             String profileImageUrl, String salutation, String role,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new UserResponse(id, email, username, countryCode, profileImageUrl, salutation,
                role, createdAt, updatedAt, 0, 0);
    }

    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean wasProfileUpdated() {
        return updatedAt != null && createdAt != null && updatedAt.isAfter(createdAt);
    }
}
