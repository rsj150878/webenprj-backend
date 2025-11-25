package at.fhtw.webenprjbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @NotBlank
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    private String username;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be ISO-2 (e.g. AT, DE)")
    private String countryCode;

    private String profileImageUrl; // optional

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
}
