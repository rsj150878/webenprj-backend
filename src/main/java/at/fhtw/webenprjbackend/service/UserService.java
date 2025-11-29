package at.fhtw.webenprjbackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.AdminUserUpdateRequest;
import at.fhtw.webenprjbackend.dto.ChangePasswordRequest;
import at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;

// TODO: Add Salutation
// TODO: Flyway migration cleartext PW -> with BCrypt only works for new registered users!!

/**
 * Service layer for user operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PROFILE_IMAGE =
            "https://example.com/default-profile.png";

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================== Register ========================
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
                passwordEncoder.encode(request.getPassword()),
                request.getCountryCode(),
                request.hasProfileImage() ? request.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE,
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

    // ======================== General Methods ========================
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


    // ======================== Self-Service for logged in users ========================
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    public UserResponse updateCurrentUserProfile(UUID userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // checks email/username uniqueness (exclude other user)
        userRepository.findByEmail(request.getEmail())
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
                });

        userRepository.findByUsername(request.getUsername())
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use.");
                });

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setCountryCode(request.getCountryCode());

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ======================== Admin-Functions ========================
    public UserResponse adminUpdateUser(UUID id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // uniqueness checks
        userRepository.findByEmail(request.getEmail())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
                });

        userRepository.findByUsername(request.getUsername())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use.");
                });

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setCountryCode(request.getCountryCode());
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        user.setActive(request.isActive());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public void adminDeleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    public UserResponse adminToggleActive(UUID id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setActive(active);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public List<UserResponse> adminSearchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        List<User> users = userRepository
                .findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
                        query, query, query
                );
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    // ======================== Helper ========================
    private UserResponse toResponse(User user) {
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
