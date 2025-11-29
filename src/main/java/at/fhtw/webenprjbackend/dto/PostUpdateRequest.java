package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for updating an existing study post.
 * All fields are optional - only provided fields will be updated.
 * Used when authenticated users want to modify their existing posts.
 * Part of the Motivise study blogging platform backend.
 */
@Schema(description = "Request to update an existing study post (all fields optional)")
public class PostUpdateRequest {

    @Size(min = 2, max = 30, message = "Subject must be between 2 and 30 characters if provided")
    @Pattern(
        regexp = "^#?[A-Za-z0-9_\\-\\s]{2,30}$",
        message = "Subject can include letters, numbers, spaces, dashes, underscores, and optional leading '#'"

    )
    @Schema(
        description = "Updated subject/topic of the study post (hashtag-style supported)", 
        example = "#SpringBootMastery",
        nullable = true
    )
    private String subject;

    @Size(min = 10, max = 500, message = "Content must be between 10 and 500 characters if provided")
    @Schema(
        description = "Updated main content describing study progress or learning update", 
        example = "Updated my understanding of dependency injection after working through more examples. Now I see how it improves testability!",
        nullable = true
    )
    private String content;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Pattern(
        regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
        message = "Must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @Schema(
        description = "Updated image URL to accompany the post (study screenshots, diagrams, etc.)", 
        example = "https://example.com/images/updated-spring-diagram.png",
        nullable = true
    )
    private String imageUrl;

    // ===============================
    // Constructors
    // ===============================

    /**
     * Default constructor for Jackson deserialization
     */
    public PostUpdateRequest() {}

    /**
     * Constructor for updating subject and content
     * @param subject Updated post subject/topic
     * @param content Updated post content
     */
    public PostUpdateRequest(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    /**
     * Constructor for updating all fields
     * @param subject Updated post subject/topic
     * @param content Updated post content
     * @param imageUrl Updated image URL
     */
    public PostUpdateRequest(String subject, String content, String imageUrl) {
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

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Checks if any field has been provided for update
     * @return true if at least one field is not null
     */
    public boolean hasUpdates() {
        return subject != null || content != null || imageUrl != null;
    }

    /**
     * Checks if the subject field is being updated
     * @return true if subject is provided and not empty
     */
    public boolean hasSubjectUpdate() {
        return subject != null && !subject.trim().isEmpty();
    }

    /**
     * Checks if the content field is being updated
     * @return true if content is provided and not empty
     */
    public boolean hasContentUpdate() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * Checks if the image URL is being updated (including removal)
     * @return true if imageUrl is explicitly provided (even if empty)
     */
    public boolean hasImageUpdate() {
        return imageUrl != null;
    }

    /**
     * Checks if the image is being removed (set to empty/null)
     * @return true if imageUrl is provided but empty
     */
    public boolean isRemovingImage() {
        return imageUrl != null && imageUrl.trim().isEmpty();
    }

    /**
     * Gets the subject formatted as a hashtag if provided
     * @return subject with leading # if not already present, null if no subject update
     */
    public String getHashtagSubject() {
        if (subject == null) return null;
        return subject.startsWith("#") ? subject : "#" + subject;
    }

    /**
     * Counts the number of fields being updated
     * @return count of non-null fields
     */
    public int getUpdateFieldCount() {
        int count = 0;
        if (hasSubjectUpdate()) count++;
        if (hasContentUpdate()) count++;
        if (hasImageUpdate()) count++;
        return count;
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "PostUpdateRequest{" +
                "subject='" + subject + '\'' +
                ", content='" + (content != null && content.length() > 50 ? 
                    content.substring(0, 50) + "..." : content) + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", updateCount=" + getUpdateFieldCount() +
                '}';
    }
}
