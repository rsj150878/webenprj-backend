package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Extended post response for admin views including moderation fields.
 */
@Schema(description = "Admin post response with moderation fields")
public record AdminPostResponse(
        @Schema(description = "Unique post identifier")
        UUID id,

        @Schema(description = "Parent post ID if this is a comment")
        UUID parentId,

        @Schema(description = "Number of direct comments on this post")
        long commentCount,

        @Schema(description = "Whether the parent post was deleted")
        boolean parentDeleted,

        @Schema(description = "Post subject/topic")
        String subject,

        @Schema(description = "Main post content")
        String content,

        @Schema(description = "Optional image URL")
        String imageUrl,

        @Schema(description = "Post creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt,

        @Schema(description = "Author user ID")
        UUID userId,

        @Schema(description = "Author username")
        String username,

        @Schema(description = "Author profile image URL")
        String userProfileImageUrl,

        @Schema(description = "Number of likes")
        long likeCount,

        @Schema(description = "Whether current user liked this post")
        boolean likedByCurrentUser,

        @Schema(description = "Number of bookmarks")
        long bookmarkCount,

        @Schema(description = "Whether current user bookmarked this post")
        boolean bookmarkedByCurrentUser,

        // Admin-specific fields
        @Schema(description = "Whether the post is active (not soft-deleted)")
        boolean active,

        @Schema(description = "Whether this is a comment (has parent)")
        boolean isComment,

        @Schema(description = "Author email for admin reference")
        String userEmail
) { }
