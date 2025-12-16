package at.fhtw.webenprjbackend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.BookmarkCollection;
import at.fhtw.webenprjbackend.entity.User;

/**
 * Repository for BookmarkCollection entity.
 * Provides data access methods for collection management.
 */
@Repository
public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, UUID> {

    /**
     * Find all collections for a user, ordered by creation date (oldest first)
     */
    List<BookmarkCollection> findByUserOrderByCreatedAtAsc(User user);

    /**
     * Find a collection by user and name
     * Used to check for duplicate collection names for the same user
     */
    Optional<BookmarkCollection> findByUserAndName(User user, String name);

    /**
     * Check if a user has a collection with a specific name
     */
    boolean existsByUserAndName(User user, String name);

    /**
     * Count total collections for a user
     */
    long countByUser(User user);
}
