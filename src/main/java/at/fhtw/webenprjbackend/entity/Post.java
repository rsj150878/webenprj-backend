package at.fhtw.webenprjbackend.entity;

// TODO: Consider switching to OffsetDateTime instead of LocalDateTime (storing timezone offset)
// TODO: Consider adding status - publish visibility, DRAFT / PUBLISHED
// TODO: Auditing (createdBy, modifiedBy) implementation

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Study post entity for the Motivise micro-blogging platform.
 * Enables students to share study notes, ask questions, and collaborate.
 *
 * @author jasmin
 * @version 0.2
 */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String subject;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // TODO: Consider switching to OffsetDateTime instead of LocalDateTime (storing timezone offset)

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===============================
    // Constructors
    // ===============================
    public Post() {}

    public Post(String subject, String content, String imageUrl, User user) {
        this.subject = subject;
        this.content = content;
        this.imageUrl = imageUrl;
        this.user = user;
    }

    // ===============================
    // Getters and Setters
    // ===============================
    public UUID getId() {
        return id;
    }
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
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}
