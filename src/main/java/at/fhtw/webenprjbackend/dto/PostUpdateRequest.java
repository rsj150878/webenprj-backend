package at.fhtw.webenprjbackend.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing post.
 * Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.1
 */
public class PostUpdateRequest {

    @Size(min = 2, max = 30,
            message = "Subject must be 2-30 characters if provided")
    private String subject;

    @Size(max = 500,
            message = "Content must not be longer than 500 characters")
    private String content;

    @Size(max = 500,
            message = "Image URL too long")
    private String imageUrl;


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
}
