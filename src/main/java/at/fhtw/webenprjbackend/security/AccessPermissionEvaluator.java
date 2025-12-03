package at.fhtw.webenprjbackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Custom permission evaluator for fine-grained method-level security in Motivise.
 *
 * <p>This evaluator enables declarative authorization using Spring Security's @PreAuthorize
 * annotation with custom permission logic. It integrates with Spring Security's expression-based
 * access control to support resource-level authorization.
 *
 * <p><b>How It Works:</b>
 * <ol>
 *   <li>Spring Security intercepts methods annotated with @PreAuthorize containing hasPermission() expressions</li>
 *   <li>The expression is evaluated, and this evaluator is invoked with the target resource and permission</li>
 *   <li>We iterate through registered {@link AccessPermission} implementations to find the right handler</li>
 *   <li>The matching permission handler checks if the current user can perform the action (e.g., "user owns post OR is admin")</li>
 *   <li>Returns true if authorized, false otherwise (resulting in AccessDeniedException)</li>
 * </ol>
 *
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * @PutMapping("/posts/{id}")
 * @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Post', 'update')")
 * public ResponseEntity<PostResponse> updatePost(@PathVariable UUID id, ...) {
 *     // Only executes if user owns the post OR is an admin
 * }
 * }</pre>
 *
 * <p><b>Registered Permission Handlers:</b>
 * <ul>
 *   <li>{@link PostAccessPermission} - Handles permissions for Post entities (update, delete)</li>
 *   <li>Future handlers can be added for other entities (e.g., Comment, Media)</li>
 * </ul>
 *
 * <p><b>Design Rationale:</b>
 * <ul>
 *   <li><b>Strategy Pattern:</b> Different permission handlers implement {@link AccessPermission}
 *       interface, allowing extensible authorization logic</li>
 *   <li><b>Loose Coupling:</b> Controllers don't contain authorization logic - it's centralized
 *       in permission classes for easier testing and maintenance</li>
 *   <li><b>Declarative Security:</b> Authorization rules are visible in controller signatures
 *       via annotations, improving code readability</li>
 *   <li><b>OR Logic:</b> Multiple permission handlers can grant access (uses bitwise OR |=)
 *       - useful when multiple paths to authorization exist</li>
 * </ul>
 *
 * <p><b>Security Note:</b> The first overload ({@link #hasPermission(Authentication, Object, Object)})
 * always returns false because we use ID-based authorization (second overload) throughout the application.
 * This prevents accidentally granting access when object-based permission checks are mistakenly used.
 *
 * @see AccessPermission
 * @see PostAccessPermission
 * @see PermissionConfiguration
 */
@Component
@RequiredArgsConstructor
public class AccessPermissionEvaluator implements PermissionEvaluator {

    private final List<AccessPermission> accessPermissions;

    /**
     * Evaluates permissions based on domain objects (not used in this implementation).
     *
     * <p><b>Always returns false</b> because we use ID-based authorization
     * via {@link #hasPermission(Authentication, Serializable, String, Object)}.
     * This method is required by the PermissionEvaluator interface.
     *
     * @param authentication the current authentication (user context)
     * @param targetDomainObject the domain object being accessed
     * @param permission the permission being checked
     * @return always false - use ID-based hasPermission instead
     */
    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission
    ) {
        return false;
    }

    /**
     * Evaluates permissions based on target resource ID and type.
     *
     * <p>This is the primary authorization method used throughout Motivise.
     * It delegates to registered {@link AccessPermission} implementations based
     * on the target type (e.g., "at.fhtw.webenprjbackend.entity.Post").
     *
     * <p><b>Example Flow:</b>
     * <ol>
     *   <li>User tries to update post with ID "abc-123"</li>
     *   <li>@PreAuthorize calls this method with (auth, "abc-123", "Post", "update")</li>
     *   <li>PostAccessPermission.supports() returns true for "Post"</li>
     *   <li>PostAccessPermission.hasPermission() checks if user owns post OR is admin</li>
     *   <li>Returns true if authorized, false otherwise</li>
     * </ol>
     *
     * @param authentication the current authentication (contains UserPrincipal)
     * @param targetId the UUID of the resource being accessed
     * @param targetType the fully qualified class name of the target entity
     * @param permission the permission being checked (e.g., "update", "delete")
     * @return true if any registered permission handler grants access, false otherwise
     */
    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission
    ) {
        boolean hasPermission = false;
        for (AccessPermission accessPermission : accessPermissions) {
            if (accessPermission.supports(authentication, targetType)) {
                hasPermission |= accessPermission.hasPermission(
                        authentication,
                        (UUID) targetId
                );
            }
        }
        return hasPermission;
    }

}
