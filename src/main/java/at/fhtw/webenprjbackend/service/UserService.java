package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: Salutation

/**
 * Service layer for user operations.
 *
 * Contains business logic for registration, data retrieval, and validation.
 * Acts as a bridge between the controller (API layer) and the repository (database layer).
 *
 * @author jasmin
 * @version 0.1
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    // Default image used when no profile picture is provided by the user
    private static final String DEFAULT_PROFILE_IMAGE =
            "https://example.com/default-profile.png";

    /**
     * Constructor injection for UserRepository.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user after validating uniqueness of email and username.
     *
     * @param request validated user registration data
     * @return created user as UserResponse DTO
     * @throws ResponseStatusException 409 if email or username already exist
     */
    public UserResponse registerUser(UserRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use.");
        }

        User newUser = new User(
                request.getEmail(),
                request.getUsername(),
                request.getPassword(), //TODO: Hash password before storing
                request.getCountryCode(),
                DEFAULT_PROFILE_IMAGE,
                Role.USER
        );

        User saved = userRepository.save(newUser);

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getUsername(),
                saved.getCountryCode(),
                saved.getProfileImageUrl(),
                saved.getRole().name(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    /**
     * Retrieves all registered users from the database.
     *
     * @return list of UserResponse objects
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getCountryCode(),
                    user.getProfileImageUrl(),
                    user.getRole().name(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            ));
        }
        return responses;
    }

    /**
     * Retrieves a single user by their UUID.
     *
     * @param id unique user identifier
     * @return user data as UserResponse
     * @throws ResponseStatusException 404 if user not found
     */
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
