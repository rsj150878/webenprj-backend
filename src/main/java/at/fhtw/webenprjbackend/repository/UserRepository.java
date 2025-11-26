package at.fhtw.webenprjbackend.repository;

import at.fhtw.webenprjbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
            String email, String username, String countryCode);

}


