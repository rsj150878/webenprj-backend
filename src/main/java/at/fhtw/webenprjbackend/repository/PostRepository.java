package at.fhtw.webenprjbackend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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


}
