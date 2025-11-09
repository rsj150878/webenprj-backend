package at.fhtw.webenprjbackend.repository;

import java.util.List;

import at.fhtw.webenprjbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import at.fhtw.webenprjbackend.entity.Post;
import java.util.UUID;


/**
 * Repository interface for Post entity operations.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 * 
 * @author Wii
 * @version 0.1
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

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

    List<Post> findBySubjectIgnoreCase(String subject);

    List<Post> findByUser(User user);


}
