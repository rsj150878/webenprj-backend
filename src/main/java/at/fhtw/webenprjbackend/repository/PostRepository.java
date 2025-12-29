package at.fhtw.webenprjbackend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.User;

/**
 * Repository interface for Post entity operations.
 * Part of the Motivise study blogging platform backend.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByOrderByCreatedAtDesc();

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Post> findByContentContainingIgnoreCase(String keyword);

    Page<Post> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Post> findBySubjectIgnoreCase(String subject, Pageable pageable);

    List<Post> findBySubjectIgnoreCase(String subject);

    List<Post> findByUser(User user);

    Page<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);

    // ========== Top-level posts only (active, no parent) ==========

    /**
     * Find all active top-level posts (not comments) ordered by creation time.
     */
    Page<Post> findByParentIsNullAndActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Search active top-level posts by content keyword.
     */
    Page<Post> findByParentIsNullAndActiveTrueAndContentContainingIgnoreCase(
            String keyword, Pageable pageable);

    /**
     * Search active top-level posts by subject.
     */
    Page<Post> findByParentIsNullAndActiveTrueAndSubjectIgnoreCase(
            String subject, Pageable pageable);

    /**
     * Following feed: active top-level posts from followed users.
     */
    Page<Post> findByParentIsNullAndActiveTrueAndUserIdInOrderByCreatedAtDesc(
            List<UUID> userIds, Pageable pageable);

    // ========== Comments for a specific post ==========

    /**
     * Get active direct comments on a post ordered by creation time ascending.
     */
    Page<Post> findByParentIdAndActiveTrueOrderByCreatedAtAsc(UUID parentId, Pageable pageable);

    /**
     * Count active comments for a single post.
     */
    long countByParentIdAndActiveTrue(UUID parentId);

    /**
     * Batch count active comments for multiple posts (prevents N+1 queries).
     */
    @Query("SELECT p.parent.id, COUNT(p) FROM Post p " +
           "WHERE p.parent.id IN :parentIds AND p.active = true " +
           "GROUP BY p.parent.id")
    List<Object[]> countCommentsByParentIds(@Param("parentIds") Collection<UUID> parentIds);

    // ========== User activity checks ==========

    /**
     * Check if user has posted (top-level, active) since a given time.
     */
    boolean existsByUserIdAndParentIsNullAndActiveTrueAndCreatedAtGreaterThanEqual(
            UUID userId, LocalDateTime since);

    /**
     * Get all active posts and comments by a user, ordered by creation time desc.
     */
    Page<Post> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

}
