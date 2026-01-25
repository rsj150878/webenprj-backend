package at.fhtw.webenprjbackend.security;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserPrincipal}.
 */
@DisplayName("UserPrincipal")
class UserPrincipalTest {

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User(
                "test@example.com",
                "testuser",
                "hashedPassword",
                "AT",
                "https://example.com/profile.png",
                Role.USER
        );
        setUserId(testUser, userId);
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
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create UserPrincipal with all fields")
        void constructor_setsAllFields() {
            // Act
            UserPrincipal principal = new UserPrincipal(
                    userId,
                    "test@example.com",
                    "testuser",
                    "hashedPassword",
                    "ROLE_USER",
                    true
            );

            // Assert
            assertThat(principal.getId()).isEqualTo(userId);
            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getUsername()).isEqualTo("testuser");
            assertThat(principal.getPassword()).isEqualTo("hashedPassword");
            assertThat(principal.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should set enabled to false when specified")
        void constructor_disabledUser() {
            // Act
            UserPrincipal principal = new UserPrincipal(
                    userId,
                    "test@example.com",
                    "testuser",
                    "hashedPassword",
                    "ROLE_USER",
                    false
            );

            // Assert
            assertThat(principal.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromUser()")
    class FromUserTests {

        @Test
        @DisplayName("should create UserPrincipal from User entity")
        void fromUser_createsCorrectPrincipal() {
            // Act
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Assert
            assertThat(principal.getId()).isEqualTo(userId);
            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getUsername()).isEqualTo("testuser");
            assertThat(principal.getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        @DisplayName("should add ROLE_ prefix to role")
        void fromUser_addsRolePrefix() {
            // Act
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Assert
            assertThat(principal.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("should handle ADMIN role correctly")
        void fromUser_adminRole() {
            // Arrange
            testUser.setRole(Role.ADMIN);

            // Act
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Assert
            assertThat(principal.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should set enabled based on user active status")
        void fromUser_setsEnabledFromActive() {
            // Arrange
            testUser.setActive(false);

            // Act
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Assert
            assertThat(principal.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasRole()")
    class HasRoleTests {

        @Test
        @DisplayName("should return true when user has the role")
        void hasRole_userHasRole_returnsTrue() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", "testuser", "password", "ROLE_ADMIN", true
            );

            // Act & Assert
            assertThat(principal.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("should return false when user does not have the role")
        void hasRole_userDoesNotHaveRole_returnsFalse() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", "testuser", "password", "ROLE_USER", true
            );

            // Act & Assert
            assertThat(principal.hasRole("ADMIN")).isFalse();
        }

        @Test
        @DisplayName("should handle lowercase role input")
        void hasRole_lowercaseInput_returnsTrue() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", "testuser", "password", "ROLE_ADMIN", true
            );

            // Act & Assert
            assertThat(principal.hasRole("admin")).isTrue();
        }

        @Test
        @DisplayName("should handle mixed case role input")
        void hasRole_mixedCaseInput_returnsTrue() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", "testuser", "password", "ROLE_USER", true
            );

            // Act & Assert
            assertThat(principal.hasRole("User")).isTrue();
        }
    }

    @Nested
    @DisplayName("getUsername()")
    class GetUsernameTests {

        @Test
        @DisplayName("should return username when set")
        void getUsername_usernameSet_returnsUsername() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", "testuser", "password", "ROLE_USER", true
            );

            // Act & Assert
            assertThat(principal.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should return email when username is null")
        void getUsername_usernameNull_returnsEmail() {
            // Arrange
            UserPrincipal principal = new UserPrincipal(
                    userId, "test@example.com", null, "password", "ROLE_USER", true
            );

            // Act & Assert
            assertThat(principal.getUsername()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("UserDetails interface methods")
    class UserDetailsInterfaceTests {

        @Test
        @DisplayName("should always return true for isAccountNonExpired")
        void isAccountNonExpired_alwaysTrue() {
            // Arrange
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Act & Assert
            assertThat(principal.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("should always return true for isAccountNonLocked")
        void isAccountNonLocked_alwaysTrue() {
            // Arrange
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Act & Assert
            assertThat(principal.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("should always return true for isCredentialsNonExpired")
        void isCredentialsNonExpired_alwaysTrue() {
            // Arrange
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Act & Assert
            assertThat(principal.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("should return correct authorities")
        void getAuthorities_returnsCorrectAuthorities() {
            // Arrange
            UserPrincipal principal = UserPrincipal.fromUser(testUser);

            // Act & Assert
            assertThat(principal.getAuthorities()).hasSize(1);
            assertThat(principal.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }
    }
}
