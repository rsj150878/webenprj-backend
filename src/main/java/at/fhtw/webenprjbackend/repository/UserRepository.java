package at.fhtw.webenprjbackend.repository;


import at.fhtw.webenprjbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;


// TODO: Optional? What's that? Neccessary?

/**
 * Repository interface for managing User entities.
 *
 * Provides CRUD operations and additional query methods using Spring Data JPA.
 *
 * @author jasmin
 * @version 0.1
 */

/**
 * ORM Teil?
 *
 */

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

}
