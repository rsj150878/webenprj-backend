package at.fhtw.webenprjbackend.security;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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