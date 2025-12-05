package at.fhtw.webenprjbackend.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.Follow;
import at.fhtw.webenprjbackend.entity.User;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerAndFollowed(User follower, User followed);

    Page<Follow> findByFollower(User follower, Pageable pageable);

    Page<Follow> findByFollowed(User followed, Pageable pageable);

    void deleteByFollowerAndFollowed(User follower, User followed);

    long countByFollowed(User followed);

    long countByFollower(User follower);
}
