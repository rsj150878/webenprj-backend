package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomUserDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User activeUser;
    private User inactiveUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        activeUser = new User(
                "test@example.com",
                "testuser",
                "hashedPassword",
                "AT",
                "https://example.com/profile.png",
                Role.USER
        );
        setUserId(activeUser, userId);

        inactiveUser = new User(
                "inactive@example.com",
                "inactiveuser",
                "hashedPassword",
                "AT",
                "https://example.com/profile.png",
                Role.USER
        );
        inactiveUser.setActive(false);
        setUserId(inactiveUser, UUID.randomUUID());
    }

    private void setUserId(User user, UUID id) {
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user id", e);
        }
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("should load user by email successfully")
        void loadByEmail_success() {
            // Arrange
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(activeUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(UserPrincipal.class);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.isEnabled()).isTrue();

            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("should load user by username when email not found")
        void loadByUsername_success() {
            // Arrange
            when(userRepository.findByEmail("testuser"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(activeUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("testuser");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(UserPrincipal.class);
            assertThat(result.getUsername()).isEqualTo("testuser");

            verify(userRepository).findByEmail("testuser");
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void loadUser_notFound_throwsException() {
            // Arrange
            when(userRepository.findByEmail("unknown@example.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsername("unknown@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("Invalid login or password");

            verify(userRepository).findByEmail("unknown@example.com");
            verify(userRepository).findByUsername("unknown@example.com");
        }

        @Test
        @DisplayName("should throw DisabledException for inactive user")
        void loadUser_inactive_throwsDisabledException() {
            // Arrange
            when(userRepository.findByEmail("inactive@example.com"))
                    .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inactive@example.com"))
                    .isInstanceOf(DisabledException.class)
                    .hasMessageContaining("account has been deactivated");

            verify(userRepository).findByEmail("inactive@example.com");
        }

        @Test
        @DisplayName("should return UserPrincipal with correct authorities for admin")
        void loadUser_admin_hasCorrectAuthorities() {
            // Arrange
            activeUser.setRole(Role.ADMIN);
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(activeUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

            // Assert
            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should return UserPrincipal with correct authorities for user")
        void loadUser_regularUser_hasCorrectAuthorities() {
            // Arrange
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(activeUser));

            // Act
            UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

            // Assert
            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }
    }
}
