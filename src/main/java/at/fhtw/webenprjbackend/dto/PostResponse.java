package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Study post information with author details")
public record PostResponse(
        @Schema(description = "Unique post identifier", example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "Post subject/topic (hashtag-style supported)", example = "#JavaLearning",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String subject,

        @Schema(description = "Main post content describing study progress or learning update",
                example = "Just finished learning about Spring Boot dependency injection. The concept of IoC is really powerful!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "Optional image URL accompanying the post",
                example = "https://example.com/images/spring-boot-diagram.png",
                nullable = true)
        String imageUrl,

        @Schema(description = "Post creation timestamp", example = "2024-11-27T10:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "Last post update timestamp", example = "2024-11-27T15:45:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt,

        @Schema(description = "ID of the user who created this post",
                example = "987fcdeb-51a2-43d7-8b9c-123456789abc",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId,

        @Schema(description = "Username of the post author for display purposes",
                example = "study_anna",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Profile image URL of the post author",
                example = "/medias/123e4567-e89b-12d3-a456-426614174000",
                nullable = true)
        String userProfileImageUrl,

        @Schema(description = "Number of likes for this post",
                example = "12",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long likeCount,

        @Schema(description = "Whether the current user liked this post",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean likedByCurrentUser,

        @Schema(description = "Number of bookmarks for this post (social proof)",
                example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long bookmarkCount,

        @Schema(description = "Whether the current user bookmarked this post",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean bookmarkedByCurrentUser
) { }
