package at.fhtw.webenprjbackend.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.AdminUserUpdateRequest;
import at.fhtw.webenprjbackend.dto.CredentialChangeRequests;
import at.fhtw.webenprjbackend.dto.ProfileUpdateResponse;
import at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.dto.AdminUserResponse;
import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;


/**
 * Service for user registration, profile management and admin user operations.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final TokenIssuer tokenIssuer;
    private final String defaultProfileImage;

    /** Constructor with DI. */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FollowRepository followRepository,
                       TokenIssuer tokenIssuer,
                       @org.springframework.beans.factory.annotation.Value("${app.user.default-profile-image:/avatar-placeholder.svg}")
                       String defaultProfileImage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.followRepository = followRepository;
        this.tokenIssuer = tokenIssuer;
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
                request.getSalutation(),
                Role.USER
        );

        User saved = userRepository.save(newUser);
        return toResponse(saved);
    }

    // ======================== General Methods ========================
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return toResponsePage(userPage);
    }

    /**
     * Get total count of registered users.
     * Used for public display on login page.
     */
    public long getUserCount() {
        return userRepository.count();
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
    public ProfileUpdateResponse updateCurrentUserProfile(UUID userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateUniqueEmailAndUsername(userId, request.getEmail(), request.getUsername());

        // Check if credentials (email or username) are changing
        boolean credentialsChanged = !user.getEmail().equals(request.getEmail())
                || !user.getUsername().equals(request.getUsername());

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setCountryCode(request.getCountryCode());
        user.setSalutation(request.getSalutation());

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User saved = userRepository.save(user);
        UserResponse userResponse = toResponse(saved);

        // new token needed - old one has stale username/email
        if (credentialsChanged) {
            String newToken = tokenIssuer.issue(saved.getId(), saved.getUsername(), saved.getRole().name());
            return new ProfileUpdateResponse(userResponse, newToken, true);
        }

        return new ProfileUpdateResponse(userResponse);
    }

    @Transactional
    public void changePassword(UUID userId, CredentialChangeRequests.PasswordChange request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public ProfileUpdateResponse changeEmail(UUID userId, CredentialChangeRequests.EmailChange request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        // Check if email is already taken by another user
        userRepository.findByEmail(request.getNewEmail())
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
                });

        // Update email
        user.setEmail(request.getNewEmail());
        User saved = userRepository.save(user);

        UserResponse userResponse = toResponse(saved);

        // Issue new token with updated info
        String newToken = tokenIssuer.issue(saved.getId(), saved.getUsername(), saved.getRole().name());
        return new ProfileUpdateResponse(userResponse, newToken, true);
    }

    /**
     * Remove avatar for current user (reset to default placeholder)
     */
    @Transactional
    public ProfileUpdateResponse removeAvatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setProfileImageUrl(defaultProfileImage);
        User saved = userRepository.save(user);

        return new ProfileUpdateResponse(toResponse(saved));
    }

    // ======================== Admin-Functions ========================
    @Transactional
    public Page<AdminUserResponse> adminGetAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toAdminResponse);
    }


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
    public AdminUserResponse adminToggleActive(UUID id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setActive(active);
        User saved = userRepository.save(user);
        return toAdminResponse(saved);
    }

    /**
     * Admin: Remove avatar for any user (reset to default placeholder)
     */
    @Transactional
    public AdminUserResponse adminRemoveAvatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setProfileImageUrl(defaultProfileImage);
        User saved = userRepository.save(user);

        return toAdminResponse(saved);
    }

    public Page<AdminUserResponse> adminSearchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return adminGetAllUsers(pageable);
        }
        return userRepository
                .findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
                        query, query, query, pageable
                ).map(this::toAdminResponse);
    }

    // ======================== Helper ========================

    /**
     * Converts a page of users to UserResponse with batch-loaded follow counts.
     * This avoids N+1 queries when fetching paginated user lists.
     */
    private Page<UserResponse> toResponsePage(Page<User> userPage) {
        List<User> users = userPage.getContent();
        if (users.isEmpty()) {
            return userPage.map(this::toResponse);
        }

        // Batch fetch follow counts (2 queries instead of 2*N)
        List<UUID> userIds = users.stream().map(User::getId).toList();
        Map<UUID, Long> followerCounts = followRepository.getFollowerCountsMap(userIds);
        Map<UUID, Long> followingCounts = followRepository.getFollowingCountsMap(userIds);

        return userPage.map(user -> new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getSalutation(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                followerCounts.getOrDefault(user.getId(), 0L),
                followingCounts.getOrDefault(user.getId(), 0L)
        ));
    }

    /**
     * Converts a single user to UserResponse (used for single-user lookups).
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getSalutation(),
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

    private AdminUserResponse toAdminResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
