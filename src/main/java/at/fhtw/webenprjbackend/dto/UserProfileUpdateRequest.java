package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for user profile updates.
 * Allows authenticated users to update their own profile information including
 * email, username, country, and profile image.
 * Used in self-service user profile management within the Motivise study platform.
 */
@Schema(description = "User profile update request for self-service profile management")
public class UserProfileUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(
        description = "Updated email address for the user account", 
        example = "anna.schmidt@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(
        description = "Updated unique username for the user (5-50 characters)",
        example = "study_anna_updated",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be exactly 2 uppercase letters (ISO 3166-1 alpha-2)")
    @Schema(
        description = "Updated ISO 3166-1 alpha-2 country code", 
        example = "DE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String countryCode;

    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
    @Pattern(
        regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
        message = "Must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @Schema(
        description = "Updated profile image URL (optional)", 
        example = "https://example.com/images/new-profile.png",
        nullable = true
    )
    private String profileImageUrl;

    // ===============================
    // Constructors
    // ===============================

    /**
     * Default constructor for Jackson deserialization
     */
    public UserProfileUpdateRequest() {}

    /**
     * Constructor for profile update without image
     * @param email Updated email address
     * @param username Updated username
     * @param countryCode Updated country code
     */
    public UserProfileUpdateRequest(String email, String username, String countryCode) {
        this.email = email;
        this.username = username;
        this.countryCode = countryCode;
    }

    /**
     * Constructor for complete profile update
     * @param email Updated email address
     * @param username Updated username
     * @param countryCode Updated country code
     * @param profileImageUrl Updated profile image URL
     */
    public UserProfileUpdateRequest(String email, String username, String countryCode, String profileImageUrl) {
        this.email = email;
        this.username = username;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getCountryCode() { 
        return countryCode; 
    }
    
    public void setCountryCode(String countryCode) { 
        this.countryCode = countryCode; 
    }

    public String getProfileImageUrl() { 
        return profileImageUrl; 
    }
    
    public void setProfileImageUrl(String profileImageUrl) { 
        this.profileImageUrl = profileImageUrl; 
    }

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Checks if a profile image is being set
     * @return true if profileImageUrl is provided and not empty
     */
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    /**
     * Validates that the email appears to be properly formatted
     * @return true if email contains @ and a domain
     */
    public boolean isValidEmailFormat() {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Checks if the username follows proper naming conventions
     * @return true if username contains only valid characters
     */
    public boolean isValidUsernameFormat() {
        return username != null && username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validates that the country code is properly formatted
     * @return true if country code is exactly 2 uppercase letters
     */
    public boolean isValidCountryCode() {
        return countryCode != null && countryCode.matches("^[A-Z]{2}$");
    }

    /**
     * Checks if all required fields are provided
     * @return true if email, username, and countryCode are all present
     */
    public boolean hasAllRequiredFields() {
        return email != null && !email.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               countryCode != null && !countryCode.trim().isEmpty();
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "UserProfileUpdateRequest{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
