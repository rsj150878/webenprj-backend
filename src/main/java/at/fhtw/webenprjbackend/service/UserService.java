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
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * Service layer for comprehensive user management in the Motivise platform.
 *
 * <p>This service provides three distinct categories of user operations:
 * <ol>
 *   <li><b>Public Registration:</b> New user account creation with email/username uniqueness validation</li>
 *   <li><b>Self-Service:</b> Authenticated users managing their own profiles and passwords</li>
 *   <li><b>Administrative:</b> Admin-only operations for user management, search, and moderation</li>
 * </ol>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>Secure Password Handling:</b> All passwords are hashed using BCrypt with 12 rounds
 *       before storage. Plaintext passwords are never stored in the database.</li>
 *   <li><b>Uniqueness Validation:</b> Email and username uniqueness is enforced both at the
 *       database level (unique constraints) and application level (validation before save)</li>
 *   <li><b>Account Status Management:</b> Users can be activated/deactivated by admins without
 *       data deletion, allowing account suspension and reinstatement</li>
 *   <li><b>Role-Based Access:</b> Supports USER and ADMIN roles with different privilege levels</li>
 *   <li><b>Profile Images:</b> Optional profile image URLs with default fallback</li>
 * </ul>
 *
 * <p><b>Design Decisions:</b>
 * <ul>
 *   <li><b>Separation of Concerns:</b> Self-service endpoints (/me) separate from admin
 *       endpoints to enforce clear permission boundaries and improve API discoverability</li>
 *   <li><b>Immutable User IDs:</b> UUID-based user IDs are never changeable, ensuring
 *       referential integrity across posts and other user-related entities</li>
 *   <li><b>Email as Login:</b> Users can log in with either email or username, providing
 *       flexibility while maintaining email uniqueness for account recovery</li>
 *   <li><b>Default Profile Images:</b> Users without uploaded images get a default avatar
 *       to ensure consistent UI rendering</li>
 * </ul>
 *
 * <p><b>Security Considerations:</b>
 * <ul>
 *   <li>Password changes require current password verification to prevent session hijacking attacks</li>
 *   <li>Admin operations check for role authorization via Spring Security's @PreAuthorize</li>
 *   <li>User searches are admin-only to prevent user enumeration attacks</li>
 *   <li>Email and username changes validate uniqueness to prevent identity conflicts</li>
 * </ul>
 *
 * @see User
 * @see UserRepository
 * @see UserController
 */
@Service
@Transactional(readOnly = true)
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
                request.hasProfileImage() ? request.getProfileImageUrl() : DEFAULT_PROFILE_IMAGE,
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
                user.getUpdatedAt()
        );
    }

}
