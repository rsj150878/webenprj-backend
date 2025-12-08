package at.fhtw.webenprjbackend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.AdminUserUpdateRequest;
import at.fhtw.webenprjbackend.dto.ChangePasswordRequest;
import at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * Service for user registration, profile management and admin user operations.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final String defaultProfileImage;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FollowRepository followRepository,
                       @org.springframework.beans.factory.annotation.Value("${app.user.default-profile-image:https://example.com/default-profile.png}")
                       String defaultProfileImage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
        this.defaultProfileImage = defaultProfileImage;
    }

    // ======================== Register ========================
    @Transactional
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
                request.hasProfileImage() ? request.getProfileImageUrl() : defaultProfileImage,
                Role.USER
        );

        User saved = userRepository.save(newUser);
        return toResponse(saved);
    }

    // ======================== General Methods ========================
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    // ======================== Self-Service for logged in users ========================
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUserProfile(UUID userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateUniqueEmailAndUsername(userId, request.getEmail(), request.getUsername());

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setCountryCode(request.getCountryCode());

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
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
    @Transactional
    public UserResponse adminUpdateUser(UUID id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateUniqueEmailAndUsername(id, request.getEmail(), request.getUsername());

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

    @Transactional
    public void adminDeleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse adminToggleActive(UUID id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setActive(active);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public Page<UserResponse> adminSearchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAllUsers(pageable);
        }
        return userRepository
                .findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
                        query, query, query, pageable
                ).map(this::toResponse);
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
                user.getUpdatedAt(),
                followRepository.countByFollowed(user),
                followRepository.countByFollower(user)
        );
    }

    private void validateUniqueEmailAndUsername(UUID excludedUserId, String email, String username) {
        userRepository.findByEmail(email)
                .filter(other -> !other.getId().equals(excludedUserId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
                });

        userRepository.findByUsername(username)
                .filter(other -> !other.getId().equals(excludedUserId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use.");
                });
    }
}
