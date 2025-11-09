package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response Data Transfer Object (DTO) for returning post information.
 * Used to send post details back to the frontend.
 *
 * Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.1
 */
public class PostResponse {

    private UUID id; // Post ID
    private String subject;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Minimal Infos from user
     *
     */
    private UUID userId;
    private String username;

    public PostResponse(UUID id, String subject, String content, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, UUID userId, String username) {
        this.id = id;
        this.subject = subject;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.username = username;
    }

    public UUID getId() { return id; }
    public String getContent() { return content; }
    public String getSubject() {
        return subject;
    }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }

}
