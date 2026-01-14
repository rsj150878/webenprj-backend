package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.LoginRequest;
import at.fhtw.webenprjbackend.dto.LoginResponse;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User(
                "test@example.com",
                "testuser",
                "hashedPassword",
                "AT",
                "https://example.com/profile.png",
                "Dr.",
                Role.USER
        );
        // Use reflection to set the ID since it's normally generated
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);

            var createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testUser, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test user fields", e);
        }
    }

    private UserPrincipal createUserPrincipal(UUID id, String email, String username, String role) {
        return new UserPrincipal(id, email, username, "hashedPassword", role, true);
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("should return token and user data on successful login with email")
        void loginWithEmail_success() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            String expectedToken = "jwt.token.here";

            UserPrincipal principal = createUserPrincipal(testUserId, "test@example.com", "testuser", "ROLE_USER");

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(tokenIssuer.issue(eq(testUserId), eq("testuser"), eq("ROLE_USER")))
                    .thenReturn(expectedToken);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(expectedToken);
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().email()).isEqualTo("test@example.com");
            assertThat(response.getUser().username()).isEqualTo("testuser");
            assertThat(response.getUser().role()).isEqualTo("USER");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenIssuer).issue(testUserId, "testuser", "ROLE_USER");
            verify(userRepository).findById(testUserId);
        }

        @Test
        @DisplayName("should return token and user data on successful login with username")
        void loginWithUsername_success() {
            // Arrange
            LoginRequest request = new LoginRequest("testuser", "password123");
            String expectedToken = "jwt.token.here";

            UserPrincipal principal = createUserPrincipal(testUserId, "test@example.com", "testuser", "ROLE_USER");

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(tokenIssuer.issue(eq(testUserId), eq("testuser"), eq("ROLE_USER")))
                    .thenReturn(expectedToken);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(expectedToken);
            assertThat(response.getUser().username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw exception on invalid credentials")
        void login_invalidCredentials_throwsException() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid credentials");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenIssuer, never()).issue(any(), anyString(), anyString());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should throw exception when user not found after authentication")
        void login_userNotFound_throwsException() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password123");

            UserPrincipal principal = createUserPrincipal(testUserId, "test@example.com", "testuser", "ROLE_USER");

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            // Token is issued before user lookup in the actual implementation
            when(tokenIssuer.issue(eq(testUserId), eq("testuser"), eq("ROLE_USER")))
                    .thenReturn("jwt.token.here");
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");

            // Token is issued before the user lookup fails
            verify(tokenIssuer).issue(testUserId, "testuser", "ROLE_USER");
        }

        @Test
        @DisplayName("should return admin role for admin user")
        void login_adminUser_returnsAdminRole() {
            // Arrange
            testUser.setRole(Role.ADMIN);
            LoginRequest request = new LoginRequest("admin@example.com", "password123");
            String expectedToken = "admin.jwt.token";

            UserPrincipal principal = createUserPrincipal(testUserId, "admin@example.com", "testuser", "ROLE_ADMIN");

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(tokenIssuer.issue(eq(testUserId), eq("testuser"), eq("ROLE_ADMIN")))
                    .thenReturn(expectedToken);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response.getUser().role()).isEqualTo("ADMIN");
            verify(tokenIssuer).issue(testUserId, "testuser", "ROLE_ADMIN");
        }
    }
}
