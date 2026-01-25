package at.fhtw.webenprjbackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SpringSecurityAuditorAware}.
 */
@DisplayName("SpringSecurityAuditorAware")
class SpringSecurityAuditorAwareTest {

    private SpringSecurityAuditorAware auditorAware;

    @BeforeEach
    void setUp() {
        auditorAware = new SpringSecurityAuditorAware();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getCurrentAuditor()")
    class GetCurrentAuditorTests {

        @Test
        @DisplayName("should return random UUID when authentication is null")
        void getCurrentAuditor_nullAuthentication_returnsRandomUUID() {
            // Arrange - no authentication set

            // Act
            Optional<UUID> result = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result).isPresent();
            // Verify it's a valid UUID (non-null)
            assertThat(result.get()).isNotNull();
        }

        @Test
        @DisplayName("should return random UUID when authentication is anonymous")
        void getCurrentAuditor_anonymousAuthentication_returnsRandomUUID() {
            // Arrange
            AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
                    "key",
                    "anonymousUser",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(anonymousToken);

            // Act
            Optional<UUID> result = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isNotNull();
        }

        @Test
        @DisplayName("should return random UUID when not authenticated")
        void getCurrentAuditor_notAuthenticated_returnsRandomUUID() {
            // Arrange - create authentication that is not authenticated
            TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password");
            token.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(token);

            // Act
            Optional<UUID> result = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isNotNull();
        }

        @Test
        @DisplayName("should return user ID when authenticated with UserPrincipal")
        void getCurrentAuditor_authenticatedUser_returnsUserId() {
            // Arrange
            UUID expectedUserId = UUID.randomUUID();
            UserPrincipal userPrincipal = new UserPrincipal(
                    expectedUserId,
                    "test@example.com",
                    "testuser",
                    "password",
                    "ROLE_USER",
                    true
            );

            Authentication authentication = new TestingAuthenticationToken(userPrincipal, null);
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Act
            Optional<UUID> result = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("should return admin user ID when authenticated as admin")
        void getCurrentAuditor_authenticatedAdmin_returnsUserId() {
            // Arrange
            UUID expectedUserId = UUID.randomUUID();
            UserPrincipal adminPrincipal = new UserPrincipal(
                    expectedUserId,
                    "admin@example.com",
                    "admin",
                    "password",
                    "ROLE_ADMIN",
                    true
            );

            Authentication authentication = new TestingAuthenticationToken(adminPrincipal, null);
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Act
            Optional<UUID> result = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("should return different UUIDs for multiple calls when unauthenticated")
        void getCurrentAuditor_multipleCallsUnauthenticated_returnsDifferentUUIDs() {
            // Arrange - no authentication

            // Act
            Optional<UUID> result1 = auditorAware.getCurrentAuditor();
            Optional<UUID> result2 = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result1).isPresent();
            assertThat(result2).isPresent();
            // Random UUIDs should be different (extremely unlikely to be the same)
            assertThat(result1.get()).isNotEqualTo(result2.get());
        }

        @Test
        @DisplayName("should return same user ID for multiple calls when authenticated")
        void getCurrentAuditor_multipleCallsAuthenticated_returnsSameUserId() {
            // Arrange
            UUID expectedUserId = UUID.randomUUID();
            UserPrincipal userPrincipal = new UserPrincipal(
                    expectedUserId,
                    "test@example.com",
                    "testuser",
                    "password",
                    "ROLE_USER",
                    true
            );

            Authentication authentication = new TestingAuthenticationToken(userPrincipal, null);
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Act
            Optional<UUID> result1 = auditorAware.getCurrentAuditor();
            Optional<UUID> result2 = auditorAware.getCurrentAuditor();

            // Assert
            assertThat(result1).isPresent();
            assertThat(result2).isPresent();
            assertThat(result1.get()).isEqualTo(result2.get());
            assertThat(result1.get()).isEqualTo(expectedUserId);
        }
    }
}
