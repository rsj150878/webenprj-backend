package at.fhtw.webenprjbackend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for creating a new study post.
 * Used when authenticated users want to share their learning progress or study updates.
 * Part of the Motivise study blogging platform backend.
 */
@Schema(description = "Request to create a new study post")
public class PostCreateRequest {

    @NotBlank(message = "Subject is required")
    @Pattern(
        regexp = "^#?[A-Za-z0-9_\\-\\s]{2,30}$",
        message = "Subject must be 2-30 characters, can include letters, numbers, spaces, dashes, underscores, and optional leading '#'"
    )
    @Schema(
        description = "Subject/topic of the study post (hashtag-style supported)", 
        example = "#JavaLearning",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String subject;

    @NotBlank(message = "Content is required")
    @Size(min = 5, max = 500, message = "Content must be between 5 and 500 characters")
    @Schema(
        description = "Main content describing the study progress or learning update", 
        example = "Just finished learning about Spring Boot dependency injection. The concept of IoC is really powerful!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Pattern(
        regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp)|/medias/[a-fA-F0-9\\-]{36})$",
        message = "Must be a valid HTTP(S) URL ending with image extension (jpg, jpeg, png, gif, webp) or internal media path (/medias/{uuid})",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @Schema(
        description = "Optional image URL to accompany the post (external HTTP(S) URL or internal /medias/{uuid} path)",
        example = "/medias/550e8400-e29b-41d4-a716-446655440000",
        nullable = true
    )
    private String imageUrl;

    @Schema(
        description = "Parent post ID when creating a comment. Leave null for top-level posts.",
        example = "123e4567-e89b-12d3-a456-426614174000",
        nullable = true
    )
    private UUID parentId;

    // Note: userId is extracted from JWT token in the controller, not sent in request body

    // ===============================
    // Constructors
    // ===============================

    /**
     * Default constructor for Jackson deserialization
     */
    public PostCreateRequest() {}

    /**
     * Constructor for creating a post with subject and content
     * @param subject The post subject/topic
     * @param content The main post content
     */
    public PostCreateRequest(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    /**
     * Constructor for creating a post with all fields
     * @param subject The post subject/topic
     * @param content The main post content
     * @param imageUrl Optional image URL
     */
    public PostCreateRequest(String subject, String content, String imageUrl) {
        this.subject = subject;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
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
     * Gets the subject formatted as a hashtag
     * @return subject with leading # if not already present
     */
    public String getHashtagSubject() {
        if (subject == null) return null;
        return subject.startsWith("#") ? subject : "#" + subject;
    }

    /**
     * Validates that content is meaningful (not just whitespace)
     * @return true if content has substantial text
     */
    public boolean hasValidContent() {
        return content != null && content.trim().length() >= 5;
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "PostCreateRequest{" +
                "subject='" + subject + '\'' +
                ", content='" + (content != null && content.length() > 50 ? 
                    content.substring(0, 50) + "..." : content) + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
