package at.fhtw.webenprjbackend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.BookmarkCollection;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostBookmark;
import at.fhtw.webenprjbackend.entity.User;

/**
 * Repository for PostBookmark entity.
 * Provides data access methods for bookmark operations including
 * bulk queries to prevent N+1 problems when fetching bookmark counts.
 */
@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, UUID> {

    /**
     * Check if a user has bookmarked a specific post
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * Delete a bookmark by user and post (idempotent unbookmark)
     */
    void deleteByUserAndPost(User user, Post post);

    /**
     * Find a bookmark by user and post
     */
    Optional<PostBookmark> findByUserAndPost(User user, Post post);

    /**
     * Get all bookmarks for a user, ordered by creation date (newest first)
     */
    Page<PostBookmark> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Get bookmarks in a specific collection, ordered by creation date
     */
    Page<PostBookmark> findByUserAndCollectionOrderByCreatedAtDesc(
        User user,
        BookmarkCollection collection,
        Pageable pageable
    );

    /**
     * Get uncategorized bookmarks (not in any collection)
     */
    Page<PostBookmark> findByUserAndCollectionIsNullOrderByCreatedAtDesc(
        User user,
        Pageable pageable
    );

    /**
     * Bulk query: Count bookmarks for multiple posts
     * Returns list of [postId, count] pairs
     * Used to prevent N+1 queries when displaying multiple posts
     */
    @Query("SELECT pb.post.id, COUNT(pb) FROM PostBookmark pb WHERE pb.post.id IN :postIds GROUP BY pb.post.id")
    List<Object[]> countBookmarksByPostIds(@Param("postIds") Collection<UUID> postIds);

    /**
     * Bulk query: Find which posts a user has bookmarked from a list of posts
     * Returns list of post IDs that the user bookmarked
     * Used to show bookmark status for multiple posts efficiently
     */
    @Query("SELECT pb.post.id FROM PostBookmark pb WHERE pb.user.id = :userId AND pb.post.id IN :postIds")
    List<UUID> findBookmarkedPostIds(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);

    /**
     * Count bookmarks in a specific collection
     */
    long countByCollection(BookmarkCollection collection);

    /**
     * Count total bookmarks for a user
     */
    long countByUser(User user);
}
