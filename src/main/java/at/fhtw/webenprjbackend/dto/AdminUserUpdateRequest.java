package at.fhtw.webenprjbackend.dto;

import at.fhtw.webenprjbackend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminUserUpdateRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 5, max = 50)
    private String username;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$")
    private String countryCode;

    private String profileImageUrl;

    private Role role;   // USER or ADMIN

    private boolean active;

    // ===============================
    // Getters and Setters
    // ===============================
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
