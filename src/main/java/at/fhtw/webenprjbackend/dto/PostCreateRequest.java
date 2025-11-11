package at.fhtw.webenprjbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request Data Transfer Object (DTO) for creating a new post.
 * Part of the Motivise study blogging platform backend.
 */

public class PostCreateRequest {
    /**
     * Subject (topic) of the post.
     */
    @NotBlank (message = "Subject is required")
    @Pattern(
            regexp = "^#?[A-Za-z0-9_-]{2,30}$",
            message = "Subject must be 2-30 characters, can include letters/numbers/-/_; optional leading with a '#' symbol"
    )
    private String subject;

    /**
     * Main content of the post.
     */
    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Content must not be longer than 500 characters")
    private String content;

    /**
     * Optional image URL attached to the post.
     */
    @Size(max = 500, message = "Image URL too long")
    private String imageUrl;

    /**
     * User ID of the author.
     * For Milestone 1 sent in request body;
     * later extracted from JWT token.
     */
    private UUID userId;


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
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
