package at.fhtw.webenprjbackend.security.permission;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Permission evaluator that delegates to {@link AccessPermission} implementations.
 */
@Component
@RequiredArgsConstructor
public class AccessPermissionEvaluator implements PermissionEvaluator {

    private final List<AccessPermission> accessPermissions;

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission
    ) {
        // object-based permissions are not used; all checks are ID-based
        return false;
    }

    /**
     * Evaluates permissions based on resource ID and type.
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
                String action = permission == null ? "" : permission.toString();

                hasPermission |= accessPermission.hasPermission(
                        authentication,
                        (UUID) targetId,
                        action
                );
            }
        }
        return hasPermission;
    }
}
