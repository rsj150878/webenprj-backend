package at.fhtw.webenprjbackend.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import at.fhtw.webenprjbackend.entity.User;
import lombok.Getter;

/**
 * Adapter between our User entity and Spring Security's UserDetails.
 * Supports login via email or username. All fields are immutable for thread-safety.
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final UUID id;
    private final String email;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
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
     * Converts User entity to UserPrincipal, adding ROLE_ prefix to match Spring Security convention.
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
