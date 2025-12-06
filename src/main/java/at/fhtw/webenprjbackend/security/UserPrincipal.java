package at.fhtw.webenprjbackend.security;

import at.fhtw.webenprjbackend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom Spring Security Principal implementation for Motivise users.
 *
 * <p>Adapts our {@link User} entity to Spring Security's {@link UserDetails} interface,
 * enabling seamless integration with Spring Security's authentication and authorization mechanisms.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>User ID Access:</b> Stores user ID for easy access in controllers,
 *       avoiding repeated database queries to retrieve the authenticated user</li>
 *   <li><b>Flexible Login:</b> Supports login with email OR username - the getUsername()
 *       method returns username primarily, with email as fallback</li>
 *   <li><b>Account Status:</b> Integrates user's active status with Spring Security's
 *       isEnabled() method for account suspension support</li>
 *   <li><b>Role Integration:</b> Automatically prefixes roles with "ROLE_" following
 *       Spring Security naming conventions</li>
 * </ul>
 *
 * <p><b>Design Rationale:</b>
 * <ul>
 *   <li><b>Immutability:</b> All fields are final to ensure thread-safety in Spring
 *       Security's authentication context</li>
 *   <li><b>Factory Method:</b> {@link #fromUser(User)} provides clean conversion from
 *       domain entity to security principal</li>
 *   <li><b>Simplified Checks:</b> Account expiration and locking are not implemented
 *       (always return true) - we use the active flag for all account status checks</li>
 * </ul>
 *
 * <p><b>Usage in Controllers:</b>
 * <pre>{@code
 * @GetMapping("/me")
 * public ResponseEntity<UserResponse> getCurrentUser(Authentication auth) {
 *     UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
 *     UUID userId = principal.getId();  // Direct access to user ID
 *     // ... use userId to fetch user data
 * }
 * }</pre>
 *
 * @see CustomUserDetailsService#loadUserByUsername(String)
 * @see User
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public class UserPrincipal implements UserDetails {

    /**
     * The user's unique identifier (UUID).
     * Provides direct access to user ID without database query.
     * Annotated with @Getter for Lombok-generated getter method.
     */
    @Getter
    private final UUID id;

    /**
     * The user's email address.
     * Used as fallback login identifier if username is not available.
     */
    private final String email;

    /**
     * The user's username.
     * Primary login identifier and display name.
     */
    private final String username;

    /**
     * BCrypt-hashed password.
     * Never stored or transmitted in plaintext.
     */
    private final String password;

    /**
     * Granted authorities (roles) for this user.
     * Typically contains one authority (e.g., "ROLE_USER" or "ROLE_ADMIN").
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Whether the user account is enabled/active.
     * Maps to the User entity's active flag.
     * When false, authentication will fail even with correct credentials.
     */
    private final boolean enabled;

    public UserPrincipal(
            UUID id,
            String email,
            String username,
            String password,
            String role,
            boolean enabled
    ) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
        this.enabled = enabled;
    }

    /**
     * Factory method to convert a User entity to UserPrincipal.
     *
     * <p>This method performs the conversion from domain model to security model:
     * <ul>
     *   <li>Extracts user ID, email, username, and hashed password</li>
     *   <li>Converts Role enum to Spring Security authority with "ROLE_" prefix</li>
     *   <li>Maps active flag to enabled status</li>
     * </ul>
     *
     * <p><b>Role Naming Convention:</b> Spring Security expects roles to be prefixed
     * with "ROLE_" (e.g., "ROLE_USER", "ROLE_ADMIN"). This method ensures consistent
     * naming for use with @PreAuthorize("hasRole('USER')") annotations.
     *
     * @param user the User entity to convert (must not be null)
     * @return a new UserPrincipal instance representing the user's security context
     */
    public static UserPrincipal fromUser(User user) {
        String roleName = "ROLE_" + user.getRole().name();
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                roleName,
                user.isActive()
        );
    }

    public String getEmail() {
        return email;
    }

    public boolean hasRole(String roleWithoutPrefix) {
        String wanted = "ROLE_" + roleWithoutPrefix.toUpperCase();
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(wanted));
    }

    // ===== UserDetails-Methods =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username != null ? username : email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
