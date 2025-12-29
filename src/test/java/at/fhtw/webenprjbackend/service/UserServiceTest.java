package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.ChangePasswordRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
            assertThat(result.getEmail()).isEqualTo("newuser@example.com");
            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getRole()).isEqualTo("USER");

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
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getFollowerCount()).isEqualTo(10L);
            assertThat(result.getFollowingCount()).isEqualTo(5L);
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
            ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123!");

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
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123!");

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
            ChangePasswordRequest request = new ChangePasswordRequest("password", "newPassword123!");

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
}
