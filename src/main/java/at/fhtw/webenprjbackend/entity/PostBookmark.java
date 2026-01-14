package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a bookmark of a post by a user.
 * Users can save posts to collections for later reference.
 * Each user can bookmark a post only once (enforced by unique constraint).
 */
@Entity
@Table(name = "post_bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class PostBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private BookmarkCollection collection;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a bookmark without collection or notes
     */
    public PostBookmark(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    /**
     * Constructor for creating a bookmark with collection and notes
     */
    public PostBookmark(User user, Post post, BookmarkCollection collection, String notes) {
        this.user = user;
        this.post = post;
        this.collection = collection;
        this.notes = notes;
    }
}
