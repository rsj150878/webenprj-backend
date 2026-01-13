package at.fhtw.webenprjbackend.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Batch query to get follower counts for multiple users.
     * Returns a list of [userId, count] pairs.
     */
    @Query("SELECT f.followed.id, COUNT(f) FROM Follow f WHERE f.followed.id IN :userIds GROUP BY f.followed.id")
    List<Object[]> countFollowersByUserIds(@Param("userIds") List<UUID> userIds);

    /**
     * Batch query to get following counts for multiple users.
     * Returns a list of [userId, count] pairs.
     */
    @Query("SELECT f.follower.id, COUNT(f) FROM Follow f WHERE f.follower.id IN :userIds GROUP BY f.follower.id")
    List<Object[]> countFollowingByUserIds(@Param("userIds") List<UUID> userIds);

    /**
     * Converts batch query results to a Map for easy lookup.
     */
    default Map<UUID, Long> getFollowerCountsMap(List<UUID> userIds) {
        return countFollowersByUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Converts batch query results to a Map for easy lookup.
     */
    default Map<UUID, Long> getFollowingCountsMap(List<UUID> userIds) {
        return countFollowingByUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
}
