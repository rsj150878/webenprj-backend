package at.fhtw.webenprjbackend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostLike;
import at.fhtw.webenprjbackend.entity.User;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByUserAndPost(User user, Post post);

    long countByPost(Post post);

    void deleteByUserAndPost(User user, Post post);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
}
