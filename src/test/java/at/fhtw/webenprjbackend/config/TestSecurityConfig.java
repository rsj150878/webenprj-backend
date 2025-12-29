package at.fhtw.webenprjbackend.config;

import at.fhtw.webenprjbackend.security.SecurityConfiguration;
import at.fhtw.webenprjbackend.security.permission.AccessPermission;
import at.fhtw.webenprjbackend.security.permission.AccessPermissionEvaluator;
import at.fhtw.webenprjbackend.security.permission.PermissionConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.List;

/**
 * Test security configuration that provides permissive security beans
 * for controller tests that disable security filters but still need method security to work.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class TestSecurityConfig {

    /**
     * Provides a PermissionEvaluator that always returns true.
     */
    @Bean
    @Primary
    public PermissionEvaluator permissionEvaluator() {
        return new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                return true;
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                return true;
            }
        };
    }

    /**
     * Provides a MethodSecurityExpressionHandler with our mock PermissionEvaluator.
     */
    @Bean
    @Primary
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator());
        return handler;
    }

    /**
     * Provides mock AccessPermissionEvaluator to satisfy PermissionConfiguration dependency.
     */
    @Bean
    @Primary
    public AccessPermissionEvaluator accessPermissionEvaluator() {
        return new AccessPermissionEvaluator(List.of());
    }

    /**
     * Provide empty list of AccessPermission implementations.
     */
    @Bean
    @Primary
    public List<AccessPermission> accessPermissions() {
        return List.of();
    }
}
