package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user-owned collection (folder) of bookmarks.
 * Collections allow users to organize their bookmarks into categories
 * like "Math Finals", "Motivation", "Study Tips", etc.
 * Each collection has a name, optional description, color, and icon for visual distinction.
 */
@Entity
@Table(name = "bookmark_collections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
public class BookmarkCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<PostBookmark> bookmarks = new ArrayList<>();

    /**
     * Constructor for creating a collection without color or icon
     */
    public BookmarkCollection(User user, String name, String description) {
        this.user = user;
        this.name = name;
        this.description = description;
    }

    /**
     * Constructor for creating a collection with all fields
     */
    public BookmarkCollection(User user, String name, String description, String color, String iconName) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.color = color;
        this.iconName = iconName;
    }
}
