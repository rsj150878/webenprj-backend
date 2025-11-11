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

// TODO: Add Salutation

/**
 * Service layer for user operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    private static final String DEFAULT_PROFILE_IMAGE =
            "https://example.com/default-profile.png";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
