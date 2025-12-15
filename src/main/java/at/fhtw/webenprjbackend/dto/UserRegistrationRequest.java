package at.fhtw.webenprjbackend.dto;

import at.fhtw.webenprjbackend.validation.ValidCountryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for user registration.
 * Used when new users create an account on the Motivise study platform.
 * All fields are required for successful registration.
 */
@Schema(description = "User registration request for creating new accounts")
public class UserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(
        description = "User's email address (must be unique)", 
        example = "anna.schmidt@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(
        description = "Unique username for the user (5-50 characters, letters, numbers, underscores only)",
        example = "study_anna",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(
        description = "User password meeting security requirements", 
        example = "Password123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    private String password;

    @NotBlank(message = "Country code is required")
    @ValidCountryCode
    @Schema(
        description = "ISO 3166-1 alpha-2 country code",
        example = "AT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String countryCode;

    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
    @Pattern(
        regexp = "^(/medias/[a-fA-F0-9-]{36}|(https?://).*\\.(jpg|jpeg|png|gif|webp|avif))$",
        message = "Must be either a media reference (/medias/{uuid}) or a valid HTTP(S) URL ending with jpg, jpeg, png, gif, webp, or avif",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @Schema(
        description = "Optional profile image URL - can be a media reference (/medias/{uuid}) or external URL",
        example = "/medias/123e4567-e89b-12d3-a456-426614174000",
        nullable = true
    )
    private String profileImageUrl;

    // ===============================
    // Constructors
    // ===============================

    /**
     * Default constructor for Jackson deserialization
     */
    public UserRegistrationRequest() {}

    /**
     * Constructor for registration without profile image
     * @param email User's email address
     * @param username Unique username
     * @param password User password
     * @param countryCode ISO country code
     */
    public UserRegistrationRequest(String email, String username, String password, String countryCode) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.countryCode = countryCode;
    }

    /**
     * Constructor for complete registration with profile image
     * @param email User's email address
     * @param username Unique username
     * @param password User password
     * @param countryCode ISO country code
     * @param profileImageUrl Optional profile image URL
     */
    public UserRegistrationRequest(String email, String username, String password, 
                                  String countryCode, String profileImageUrl) {
        this.email = email;
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
     * Checks if a profile image is provided during registration
     * @return true if profileImageUrl is provided and not empty
     */
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    /**
     * Validates that all required fields are provided
     * @return true if email, username, password, and countryCode are all present
     */
    public boolean hasAllRequiredFields() {
        return email != null && !email.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               countryCode != null && !countryCode.trim().isEmpty();
    }

    /**
     * Checks if the email appears to be properly formatted
     * @return true if email contains @ and a domain
     */
    public boolean isValidEmailFormat() {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Validates that the username follows proper naming conventions
     * @return true if username contains only valid characters
     */
    public boolean isValidUsernameFormat() {
        return username != null && username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validates password complexity requirements
     * @return true if password meets security requirements
     */
    public boolean isValidPasswordFormat() {
        return password != null && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$");
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +  // Don't log actual password
                ", countryCode='" + countryCode + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
