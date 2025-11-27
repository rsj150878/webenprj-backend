package at.fhtw.webenprjbackend.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import at.fhtw.webenprjbackend.entity.User;

public class UserPrincipal implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    // ===============================
    // Constructor
    // ===============================

    public UserPrincipal(User user) {
        this.user = user;
        String roleName = "ROLE_" + user.getRole().name(); // USER -> ROLE_USER
        this.authorities = List.of(new SimpleGrantedAuthority(roleName));
    }

    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(user);
    }


    // ===============================
    // Getters
    // ===============================
    public UUID getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }

    // ===============================
    // Overrides (UserDetails)
    // ===============================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // we use email as "username" for security
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.isActive(); }

}
