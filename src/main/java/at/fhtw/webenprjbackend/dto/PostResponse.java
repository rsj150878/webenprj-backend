package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for sending post data to the frontend.
 * Part of the Motivise study blogging platform backend.
 */
public class PostResponse {

    private final UUID id; // Post ID
    private final String subject;
    private final String content;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Minimal Infos from user
     *
     */
    private final UUID userId;
    private final String username;

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

    // ===============================
    // Getters
    // ===============================
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
