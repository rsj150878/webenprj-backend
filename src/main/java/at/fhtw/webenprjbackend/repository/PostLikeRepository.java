package at.fhtw.webenprjbackend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostLike;
import at.fhtw.webenprjbackend.entity.User;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByUserAndPost(User user, Post post);

    long countByPost(Post post);

    void deleteByUserAndPost(User user, Post post);

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    @Query("select pl.post.id, count(pl) from PostLike pl where pl.post.id in :postIds group by pl.post.id")
    java.util.List<Object[]> countLikesByPostIds(@Param("postIds") java.util.Collection<UUID> postIds);

    @Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
    java.util.List<UUID> findLikedPostIds(@Param("userId") UUID userId, @Param("postIds") java.util.Collection<UUID> postIds);
}
