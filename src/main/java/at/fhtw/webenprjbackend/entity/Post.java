package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Study post entity for the Motivise micro-blogging platform.
 *
 * <p>Posts are the primary content type in Motivise, enabling students to:
 * <ul>
 *   <li>Share study progress and learning milestones</li>
 *   <li>Ask questions and seek help from the community</li>
 *   <li>Organize content by subject/topic (hashtag-style)</li>
 *   <li>Attach images (diagrams, screenshots, notes)</li>
 * </ul>
 *
 * <p><b>Database Schema:</b>
 * <ul>
 *   <li>Table: {@code posts}</li>
 *   <li>Primary Key: {@code id} (UUID)</li>
 *   <li>Foreign Key: {@code user_id} references {@code users(id)}</li>
 *   <li>Indexes: Automatic on primary key and foreign key columns</li>
 * </ul>
 *
 * <p><b>Content Limits:</b>
 * <ul>
 *   <li>Subject: 30 characters (similar to Twitter hashtags)</li>
 *   <li>Content: 500 characters (micro-blogging format)</li>
 *   <li>Image URL: 500 characters (optional attachment)</li>
 * </ul>
 *
 * <p><b>Future Enhancements (Technical Debt):</b>
 * <ul>
 *   <li><b>OffsetDateTime:</b> Consider switching from LocalDateTime to OffsetDateTime
 *       for multi-timezone support. Current implementation works for single-timezone
 *       deployments. Migration would require Flyway migration V5+.</li>
 *   <li><b>Publish Status:</b> Consider adding DRAFT/PUBLISHED status for post visibility
 *       control, allowing users to prepare posts before sharing publicly.</li>
 *   <li><b>Auditing:</b> Consider implementing Spring Data JPA auditing for createdBy
 *       and modifiedBy tracking, useful for admin moderation features.</li>
 * </ul>
 *
 * @see User
 * @see PostService
 * @see PostController
 */
@Entity
@Table(name = "posts")
public class Post {

    /**
     * Unique identifier for the post (UUID v4).
     * Generated automatically upon post creation.
     * Immutable and used for referencing posts in update/delete operations.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Subject or topic of the post (hashtag-style categorization).
     * Limited to 30 characters for concise, scannable topics.
     *
     * <p><b>Storage Format:</b> Stored WITHOUT leading '#' in the database for
     * consistency and search efficiency. The '#' is added back in API responses
     * for frontend display purposes.
     *
     * <p>Examples: "JavaLearning", "webengineering", "database"
     *
     * @see PostService#normalizeSubject(String)
     * @see PostService#mapToResponse(Post)
     */
    @Column(nullable = false, length = 30)
    private String subject;

    /**
     * Main content of the study post.
     * Limited to 500 characters to encourage concise, focused updates.
     *
     * <p>Content can include:
     * <ul>
     *   <li>Learning progress updates</li>
     *   <li>Questions for the community</li>
     *   <li>Study tips and insights</li>
     *   <li>Links to resources (within character limit)</li>
     * </ul>
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * Optional URL pointing to an image attachment.
     * Supports study-related visuals like diagrams, screenshots, or notes.
     *
     * <p>Validated for HTTPS protocol and common image formats (jpg, png, gif, webp).
     * Images are not stored directly - only URLs to external or uploaded images.
     *
     * <p>May be {@code null} if post has no image attachment.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Author of this post (mandatory relationship).
     * Links to the User who created this post.
     *
     * <p><b>Relationship Details:</b>
     * <ul>
     *   <li>Type: Many-to-One (many posts belong to one user)</li>
     *   <li>Cascade: None (deleting user requires handling posts separately)</li>
     *   <li>Fetch: Eager (default) - user info often needed with post data</li>
     *   <li>Optional: false (every post must have an author)</li>
     * </ul>
     *
     * <p>This relationship enables features like:
     * <ul>
     *   <li>User profiles showing all their posts</li>
     *   <li>Authorization checks (user can edit/delete own posts)</li>
     *   <li>Attribution (showing author username with each post)</li>
     * </ul>
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Timestamp when the post was created.
     * Automatically set by Hibernate on first persist - cannot be modified.
     * Used for chronological ordering and displaying post age to users.
     *
     * <p><b>Note:</b> Uses LocalDateTime (server timezone). For future multi-timezone
     * support, consider migration to OffsetDateTime.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last post modification.
     * Automatically updated by Hibernate on every save operation.
     * Used to show "edited" status and track modification history.
     */
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
