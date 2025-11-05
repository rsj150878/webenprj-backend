package at.fhtw.webenprjbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.Post;

/**
 * Repository interface for Post entity operations.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 * 
 * @author Wii
 * @version 0.1
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all posts ordered by creation date (newest first).
     * Perfect for displaying the latest study posts on the main feed.
     * 
     * @return List of posts in descending order by creation date
     */
    List<Post> findAllByOrderByCreatedAtDesc();

    /**
     * Search posts by content containing a keyword (case-insensitive).
     * Useful for finding study posts about specific topics.
     * 
     * @param keyword The search term to look for in post content
     * @return List of posts containing the keyword
     */
    List<Post> findByContentContainingIgnoreCase(String keyword);

    /**
     * Find posts by title containing a keyword (case-insensitive).
     * Helps students find posts about specific subjects.
     * 
     * @param keyword The search term to look for in post titles
     * @return List of posts with titles containing the keyword
     */
    List<Post> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Custom query to search posts by both title and content.
     * More flexible search for finding relevant study materials.
     * 
     * @param keyword The search term
     * @return List of posts matching in either title or content
     */
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByTitleOrContent(@Param("keyword") String keyword);
}
