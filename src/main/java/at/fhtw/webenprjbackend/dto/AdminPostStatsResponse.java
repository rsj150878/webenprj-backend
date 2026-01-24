package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Statistics response for admin posts dashboard.
 */
@Schema(description = "Admin post statistics")
public record AdminPostStatsResponse(
        @Schema(description = "Total number of posts (excluding comments)")
        long totalPosts,

        @Schema(description = "Number of active posts")
        long activePosts,

        @Schema(description = "Number of deleted posts")
        long deletedPosts,

        @Schema(description = "Total number of comments")
        long totalComments,

        @Schema(description = "Number of active comments")
        long activeComments,

        @Schema(description = "Number of deleted comments")
        long deletedComments
) { }
