package at.fhtw.webenprjbackend.repository;

import java.util.List;
import at.fhtw.webenprjbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import at.fhtw.webenprjbackend.entity.Post;
import java.util.UUID;

/**
 * Repository interface for Post entity operations.
 * Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.2
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByContentContainingIgnoreCase(String keyword);

    List<Post> findBySubjectIgnoreCase(String subject);

    List<Post> findByUser(User user);


}
