package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response Data Transfer Object (DTO) for returning post information to clients.
 * Contains complete post data along with minimal user information for display purposes.
 * Part of the Motivise study blogging platform backend.
 */
@Schema(description = "Study post information with author details")
public class PostResponse {

    @Schema(
        description = "Unique post identifier", 
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final UUID id;

    @Schema(
        description = "Post subject/topic (hashtag-style supported)", 
        example = "#JavaLearning",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String subject;

    @Schema(
        description = "Main post content describing study progress or learning update", 
        example = "Just finished learning about Spring Boot dependency injection. The concept of IoC is really powerful!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String content;

    @Schema(
        description = "Optional image URL accompanying the post", 
        example = "https://example.com/images/spring-boot-diagram.png",
        nullable = true
    )
    private final String imageUrl;

    @Schema(
        description = "Post creation timestamp", 
        example = "2024-11-27T10:30:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final LocalDateTime createdAt;

    @Schema(
        description = "Last post update timestamp", 
        example = "2024-11-27T15:45:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final LocalDateTime updatedAt;

    // ===============================
    // Author Information (Minimal for Display)
    // ===============================

    @Schema(
        description = "ID of the user who created this post", 
        example = "987fcdeb-51a2-43d7-8b9c-123456789abc",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final UUID userId;

    @Schema(
        description = "Username of the post author for display purposes", 
        example = "study_anna",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String username;

    @Schema(
        description = "Number of likes for this post",
        example = "12",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final long likeCount;

    @Schema(
        description = "Whether the current user liked this post",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final boolean likedByCurrentUser;

    // ===============================
    // Constructor
    // ===============================

    /**
     * Creates a complete post response with all information
     * @param id Unique post identifier
     * @param subject Post subject/topic
     * @param content Main post content
     * @param imageUrl Optional image URL
     * @param createdAt Post creation timestamp
     * @param updatedAt Last update timestamp
     * @param userId ID of the post author
     * @param username Username of the post author
     * @param likeCount Total likes
     * @param likedByCurrentUser Whether current user liked the post
     */
    public PostResponse(UUID id, String subject, String content, String imageUrl, 
                       LocalDateTime createdAt, LocalDateTime updatedAt, 
                       UUID userId, String username,
                       long likeCount, boolean likedByCurrentUser) {
        this.id = id;
        this.subject = subject;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.username = username;
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    // ===============================
    // Getters
    // ===============================

    public UUID getId() { 
        return id; 
    }

    public String getSubject() { 
        return subject; 
    }

    public String getContent() { 
        return content; 
    }

    public String getImageUrl() { 
        return imageUrl; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }

    public UUID getUserId() { 
        return userId; 
    }

    public String getUsername() { 
        return username; 
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Checks if the post has an associated image
     * @return true if imageUrl is provided and not empty
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * Checks if the post was updated after creation
     * @return true if updatedAt is after createdAt
     */
    public boolean wasUpdated() {
        return updatedAt != null && createdAt != null && 
               updatedAt.isAfter(createdAt);
    }

    /**
     * Gets the subject formatted as a hashtag
     * @return subject with leading # if not already present
     */
    public String getHashtagSubject() {
        if (subject == null) return null;
        return subject.startsWith("#") ? subject : "#" + subject;
    }

    /**
     * Gets a shortened version of the content for preview purposes
     * @param maxLength Maximum number of characters to include
     * @return Truncated content with "..." if longer than maxLength
     */
    public String getContentPreview(int maxLength) {
        if (content == null) return null;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Checks if this post belongs to the specified user
     * @param userIdToCheck User ID to check ownership against
     * @return true if the post belongs to the user
     */
    public boolean belongsToUser(UUID userIdToCheck) {
        return userId != null && userId.equals(userIdToCheck);
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "PostResponse{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", content='" + getContentPreview(50) + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", likeCount=" + likeCount +
                ", likedByCurrentUser=" + likedByCurrentUser +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PostResponse that = (PostResponse) obj;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
