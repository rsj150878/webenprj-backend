package at.fhtw.webenprjbackend.security.permission;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AccessPermissionEvaluator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessPermissionEvaluator")
class AccessPermissionEvaluatorTest {

    @Mock
    private AccessPermission mockPermission1;

    @Mock
    private AccessPermission mockPermission2;

    private AccessPermissionEvaluator evaluator;
    private Authentication authentication;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        principal = new UserPrincipal(
                userId,
                "test@example.com",
                "testuser",
                "hashedPassword",
                "ROLE_USER",
                true
        );
        authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    @Nested
    @DisplayName("hasPermission(Authentication, Object, Object)")
    class ObjectBasedPermissionTests {

        @Test
        @DisplayName("should return false for object-based permission checks")
        void objectBasedPermission_returnsFalse() {
            // Arrange
            evaluator = new AccessPermissionEvaluator(Collections.emptyList());

            // Act
            boolean result = evaluator.hasPermission(authentication, new Object(), "read");

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermission(Authentication, Serializable, String, Object)")
    class IdBasedPermissionTests {

        @Test
        @DisplayName("should return false when no permissions support the type")
        void noSupportingPermission_returnsFalse() {
            // Arrange
            when(mockPermission1.supports(authentication, "SomeType")).thenReturn(false);
            evaluator = new AccessPermissionEvaluator(List.of(mockPermission1));
            UUID resourceId = UUID.randomUUID();

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, "SomeType", "read");

            // Assert
            assertThat(result).isFalse();
            verify(mockPermission1).supports(authentication, "SomeType");
            verify(mockPermission1, never()).hasPermission(any(), any(), anyString());
        }

        @Test
        @DisplayName("should return true when supporting permission grants access")
        void supportingPermissionGrantsAccess_returnsTrue() {
            // Arrange
            String targetType = Post.class.getName();
            UUID resourceId = UUID.randomUUID();

            when(mockPermission1.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission1.hasPermission(authentication, resourceId, "read")).thenReturn(true);

            evaluator = new AccessPermissionEvaluator(List.of(mockPermission1));

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, targetType, "read");

            // Assert
            assertThat(result).isTrue();
            verify(mockPermission1).supports(authentication, targetType);
            verify(mockPermission1).hasPermission(authentication, resourceId, "read");
        }

        @Test
        @DisplayName("should return false when supporting permission denies access")
        void supportingPermissionDeniesAccess_returnsFalse() {
            // Arrange
            String targetType = Post.class.getName();
            UUID resourceId = UUID.randomUUID();

            when(mockPermission1.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission1.hasPermission(authentication, resourceId, "delete")).thenReturn(false);

            evaluator = new AccessPermissionEvaluator(List.of(mockPermission1));

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, targetType, "delete");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should check multiple permissions and return true if any grants access")
        void multiplePermissions_anyGrantsAccess_returnsTrue() {
            // Arrange
            String targetType = Post.class.getName();
            UUID resourceId = UUID.randomUUID();

            when(mockPermission1.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission1.hasPermission(authentication, resourceId, "read")).thenReturn(false);

            when(mockPermission2.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission2.hasPermission(authentication, resourceId, "read")).thenReturn(true);

            evaluator = new AccessPermissionEvaluator(Arrays.asList(mockPermission1, mockPermission2));

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, targetType, "read");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should handle null permission action")
        void nullPermissionAction_handledCorrectly() {
            // Arrange
            String targetType = Post.class.getName();
            UUID resourceId = UUID.randomUUID();

            when(mockPermission1.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission1.hasPermission(authentication, resourceId, "")).thenReturn(true);

            evaluator = new AccessPermissionEvaluator(List.of(mockPermission1));

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, targetType, null);

            // Assert
            assertThat(result).isTrue();
            verify(mockPermission1).hasPermission(authentication, resourceId, "");
        }

        @Test
        @DisplayName("should only check permissions that support the target type")
        void onlyChecksMatchingPermissions() {
            // Arrange
            String targetType = Post.class.getName();
            UUID resourceId = UUID.randomUUID();

            when(mockPermission1.supports(authentication, targetType)).thenReturn(false);
            when(mockPermission2.supports(authentication, targetType)).thenReturn(true);
            when(mockPermission2.hasPermission(authentication, resourceId, "read")).thenReturn(true);

            evaluator = new AccessPermissionEvaluator(Arrays.asList(mockPermission1, mockPermission2));

            // Act
            boolean result = evaluator.hasPermission(authentication, resourceId, targetType, "read");

            // Assert
            assertThat(result).isTrue();
            verify(mockPermission1).supports(authentication, targetType);
            verify(mockPermission1, never()).hasPermission(any(), any(), anyString());
            verify(mockPermission2).supports(authentication, targetType);
            verify(mockPermission2).hasPermission(authentication, resourceId, "read");
        }
    }
}
