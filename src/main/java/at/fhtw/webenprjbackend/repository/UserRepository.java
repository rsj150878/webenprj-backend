package at.fhtw.webenprjbackend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.fhtw.webenprjbackend.entity.User;

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

    Page<User> findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
            String email, String username, String countryCode, Pageable pageable);

}


