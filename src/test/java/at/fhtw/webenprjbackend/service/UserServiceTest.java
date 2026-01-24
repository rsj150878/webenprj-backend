package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.*;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private TokenIssuer tokenIssuer;

    private UserService userService;

    private static final String DEFAULT_PROFILE_IMAGE = "https://example.com/default-profile.png";

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                followRepository,
                tokenIssuer,
                DEFAULT_PROFILE_IMAGE
        );

        testUserId = UUID.randomUUID();
        testUser = createTestUser(testUserId, "testuser", "test@example.com");
    }

    private User createTestUser(UUID id, String username, String email) {
        User user = new User(email, username, "hashedPassword", "AT",
                "https://example.com/profile.png", "Dr.", Role.USER);
        setField(user, "id", id);
        setField(user, "createdAt", LocalDateTime.now());
        return user;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    @Nested
    @DisplayName("registerUser()")
    class RegisterUserTests {

        @Test
        @DisplayName("should register user successfully")
        void registerUser_success() {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "newuser@example.com", "newuser", "Password123!", "AT"
            );

            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                setField(saved, "id", UUID.randomUUID());
                setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);

            // Act
            UserResponse result = userService.registerUser(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("newuser@example.com");
            assertThat(result.username()).isEqualTo("newuser");
            assertThat(result.role()).isEqualTo("USER");

            verify(passwordEncoder).encode("Password123!");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should use default profile image when none provided")
        void registerUser_usesDefaultProfileImage() {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "newuser@example.com", "newuser", "Password123!", "AT"
            );
            // No profile image URL set

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                // Verify default profile image is used
                assertThat(saved.getProfileImageUrl()).isEqualTo(DEFAULT_PROFILE_IMAGE);
                setField(saved, "id", UUID.randomUUID());
                setField(saved, "createdAt", LocalDateTime.now());
                return saved;
            });
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);

            // Act
            userService.registerUser(request);

            // Assert
            verify(userRepository).save(argThat(user ->
                    user.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE)));
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void registerUser_emailExists_throwsException() {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "existing@example.com", "newuser", "Password123!", "AT"
            );

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Email is already in use");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void registerUser_usernameExists_throwsException() {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "newuser@example.com", "existinguser", "Password123!", "AT"
            );

            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Username is already in use");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when found")
        void getUserById_found_returnsUser() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(followRepository.countByFollowed(testUser)).thenReturn(10L);
            when(followRepository.countByFollower(testUser)).thenReturn(5L);

            // Act
            UserResponse result = userService.getUserById(testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.followerCount()).isEqualTo(10L);
            assertThat(result.followingCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserById_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("should change password successfully")
        void changePassword_success() {
            // Arrange
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange("oldPassword", "newPassword123!");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123!")).thenReturn("newHashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.changePassword(testUserId, request);

            // Assert
            verify(passwordEncoder).matches("oldPassword", "hashedPassword");
            verify(passwordEncoder).encode("newPassword123!");
            verify(userRepository).save(argThat(user ->
                    user.getPassword().equals("newHashedPassword")));
        }

        @Test
        @DisplayName("should throw exception when current password is incorrect")
        void changePassword_wrongCurrentPassword_throwsException() {
            // Arrange
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange("wrongPassword", "newPassword123!");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Current password is incorrect");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void changePassword_userNotFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange("password", "newPassword123!");

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(nonExistentId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("adminDeleteUser()")
    class AdminDeleteUserTests {

        @Test
        @DisplayName("should delete user successfully")
        void adminDeleteUser_success() {
            // Arrange
            when(userRepository.existsById(testUserId)).thenReturn(true);

            // Act
            userService.adminDeleteUser(testUserId);

            // Assert
            verify(userRepository).deleteById(testUserId);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void adminDeleteUser_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.existsById(nonExistentId)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.adminDeleteUser(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return current user")
        void getCurrentUser_returnsUser() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(followRepository.countByFollowed(testUser)).thenReturn(5L);
            when(followRepository.countByFollower(testUser)).thenReturn(3L);

            // Act
            UserResponse result = userService.getCurrentUser(testUserId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getCurrentUser_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getCurrentUser(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateCurrentUserProfile()")
    class UpdateCurrentUserProfileTests {

        @Test
        @DisplayName("should update profile without credential change")
        void updateProfile_noCredentialChange_returnsWithoutToken() {
            // Arrange
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "DE"
            );
            request.setSalutation("Prof.");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);

            // Act
            ProfileUpdateResponse result = userService.updateCurrentUserProfile(testUserId, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCredentialsChanged()).isFalse();
        }

        @Test
        @DisplayName("should issue new token when credentials change")
        void updateProfile_credentialChange_returnsWithToken() {
            // Arrange
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "newemail@example.com", "testuser", "AT"
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);
            when(tokenIssuer.issue(any(), anyString(), anyString())).thenReturn("newToken");

            // Act
            ProfileUpdateResponse result = userService.updateCurrentUserProfile(testUserId, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCredentialsChanged()).isTrue();
            assertThat(result.getToken()).isEqualTo("newToken");
        }

        @Test
        @DisplayName("should throw exception when email already in use")
        void updateProfile_emailConflict_throwsException() {
            // Arrange
            User otherUser = createTestUser(UUID.randomUUID(), "other", "other@example.com");
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "other@example.com", "testuser", "AT"
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.updateCurrentUserProfile(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Email is already in use");
        }

        @Test
        @DisplayName("should throw exception when username already in use")
        void updateProfile_usernameConflict_throwsException() {
            // Arrange
            User otherUser = createTestUser(UUID.randomUUID(), "takenuser", "other@example.com");
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "takenuser", "AT"
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("takenuser")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.updateCurrentUserProfile(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Username is already in use");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void updateProfile_userNotFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT"
            );

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.updateCurrentUserProfile(nonExistentId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("changeEmail()")
    class ChangeEmailTests {

        @Test
        @DisplayName("should change email successfully")
        void changeEmail_success_returnsWithNewToken() {
            // Arrange
            CredentialChangeRequests.EmailChange request =
                    new CredentialChangeRequests.EmailChange("newemail@example.com", "hashedPassword");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("hashedPassword", "hashedPassword")).thenReturn(true);
            when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);
            when(tokenIssuer.issue(any(), anyString(), anyString())).thenReturn("newToken");

            // Act
            ProfileUpdateResponse result = userService.changeEmail(testUserId, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCredentialsChanged()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when password is incorrect")
        void changeEmail_wrongPassword_throwsException() {
            // Arrange
            CredentialChangeRequests.EmailChange request =
                    new CredentialChangeRequests.EmailChange("newemail@example.com", "wrongPassword");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.changeEmail(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Current password is incorrect");
        }

        @Test
        @DisplayName("should throw exception when email already in use")
        void changeEmail_emailConflict_throwsException() {
            // Arrange
            User otherUser = createTestUser(UUID.randomUUID(), "other", "taken@example.com");
            CredentialChangeRequests.EmailChange request =
                    new CredentialChangeRequests.EmailChange("taken@example.com", "hashedPassword");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("hashedPassword", "hashedPassword")).thenReturn(true);
            when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.changeEmail(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Email is already in use");
        }
    }

    @Nested
    @DisplayName("removeAvatar()")
    class RemoveAvatarTests {

        @Test
        @DisplayName("should reset avatar to default")
        void removeAvatar_success() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);

            // Act
            ProfileUpdateResponse result = userService.removeAvatar(testUserId);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE)));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void removeAvatar_userNotFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.removeAvatar(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return paginated users")
        void getAllUsers_returnsPaginatedUsers() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(followRepository.getFollowerCountsMap(any())).thenReturn(Map.of());
            when(followRepository.getFollowingCountsMap(any())).thenReturn(Map.of());

            // Act
            Page<UserResponse> result = userService.getAllUsers(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getUserCount()")
    class GetUserCountTests {

        @Test
        @DisplayName("should return total user count")
        void getUserCount_returnsCount() {
            // Arrange
            when(userRepository.count()).thenReturn(100L);

            // Act
            long result = userService.getUserCount();

            // Assert
            assertThat(result).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("adminGetAllUsers()")
    class AdminGetAllUsersTests {

        @Test
        @DisplayName("should return admin user page")
        void adminGetAllUsers_returnsAdminUserPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // Act
            Page<AdminUserResponse> result = userService.adminGetAllUsers(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("adminUpdateUser()")
    class AdminUpdateUserTests {

        @Test
        @DisplayName("should update user successfully")
        void adminUpdateUser_success() {
            // Arrange
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                    "updated@example.com", "updateduser", "DE", null, Role.ADMIN, true
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(followRepository.countByFollowed(any())).thenReturn(0L);
            when(followRepository.countByFollower(any())).thenReturn(0L);

            // Act
            UserResponse result = userService.adminUpdateUser(testUserId, request);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void adminUpdateUser_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                    "test@example.com", "testuser", "AT", null, null, true
            );

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.adminUpdateUser(nonExistentId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should throw exception when email conflict in admin update")
        void adminUpdateUser_emailConflict_throwsException() {
            // Arrange
            User otherUser = createTestUser(UUID.randomUUID(), "other", "taken@example.com");
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                    "taken@example.com", "testuser", "AT", null, null, true
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.adminUpdateUser(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Email is already in use");
        }

        @Test
        @DisplayName("should throw exception when username conflict in admin update")
        void adminUpdateUser_usernameConflict_throwsException() {
            // Arrange
            User otherUser = createTestUser(UUID.randomUUID(), "takenuser", "other@example.com");
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                    "test@example.com", "takenuser", "AT", null, null, true
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("takenuser")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.adminUpdateUser(testUserId, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Username is already in use");
        }
    }

    @Nested
    @DisplayName("adminToggleActive()")
    class AdminToggleActiveTests {

        @Test
        @DisplayName("should activate user")
        void adminToggleActive_activate_success() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AdminUserResponse result = userService.adminToggleActive(testUserId, true);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(User::isActive));
        }

        @Test
        @DisplayName("should deactivate user")
        void adminToggleActive_deactivate_success() {
            // Arrange
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AdminUserResponse result = userService.adminToggleActive(testUserId, false);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user -> !user.isActive()));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void adminToggleActive_notFound_throwsException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.adminToggleActive(nonExistentId, true))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("adminSearchUsers()")
    class AdminSearchUsersTests {

        @Test
        @DisplayName("should search users by query")
        void adminSearchUsers_withQuery_returnsMatches() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrCountryCodeContainingIgnoreCase(
                    "test", "test", "test", pageable)).thenReturn(userPage);

            // Act
            Page<AdminUserResponse> result = userService.adminSearchUsers("test", pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return all users when query is blank")
        void adminSearchUsers_blankQuery_returnsAll() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // Act
            Page<AdminUserResponse> result = userService.adminSearchUsers("   ", pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).findAll(pageable);
        }
    }
}
