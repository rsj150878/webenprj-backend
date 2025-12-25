package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Represents a study post created by a user.
 *
 * <p>Posts contain a short subject, text content and an optional image URL.
 * Each post is linked to exactly one author and has automatic
 * creation and update timestamps.
 */
@Entity
@Table(name = "posts")
public class Post {

    /**
     * Primary key of the post.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Short subject or topic of the post (max. 30 characters).
     */
    @Column(nullable = false, length = 30)
    private String subject;

    /**
     * Main text content of the post (max. 500 characters).
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * Optional URL to an image related to the post.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Author of this post. Must not be {@code null}.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Parent post if this is a comment/reply. {@code null} for top-level posts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Post parent;

    /**
     * Child comments on this post, ordered by creation time ascending.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Post> comments = new ArrayList<>();

    /**
     * Soft delete flag. When {@code false}, the post is considered deleted
     * but remains in the database for reference by child comments.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Timestamp when the post was created. Set automatically on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update. Updated automatically on change.
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Post getParent() {
        return parent;
    }

    public void setParent(Post parent) {
        this.parent = parent;
    }

    public List<Post> getComments() {
        return comments;
    }

    public void setComments(List<Post> comments) {
        this.comments = comments;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Helper method to check if this post is a comment (has a parent).
     */
    public boolean isComment() {
        return parent != null;
    }

    /**
     * Helper method to check if this post has been soft-deleted.
     */
    public boolean isDeleted() {
        return !active;
    }

}
