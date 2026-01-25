package at.fhtw.webenprjbackend.dto;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.validation.ValidCountryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for admin-level user updates.
 * Allows administrators to modify any user's profile information, role, and account status.
 * This DTO includes fields that regular users cannot modify themselves.
 */
@Schema(description = "Admin user update request with full profile control")
public class AdminUserUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
        description = "User's email address", 
        example = "anna.schmidt@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(
        description = "Unique username for the user (5-50 characters)",
        example = "study_anna",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

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
        description = "URL to user's profile image - can be a media reference (/medias/{uuid}) or external URL",
        example = "/medias/123e4567-e89b-12d3-a456-426614174000",
        nullable = true
    )
    private String profileImageUrl;

    @Schema(
        description = "User role determining access permissions", 
        example = "USER",
        allowableValues = {"USER", "ADMIN"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Role role;   // USER or ADMIN

    @Schema(
        description = "Whether the user account is active (can login)", 
        example = "true",
        defaultValue = "true"
    )
    private boolean active;

    // Constructors

    /**
     * Default constructor for Jackson deserialization
     */
    public AdminUserUpdateRequest() {}

    /**
     * Constructor with all fields
     * @param email User's email address
     * @param username Unique username
     * @param countryCode ISO country code
     * @param profileImageUrl Profile image URL
     * @param role User role (USER or ADMIN)
     * @param active Account active status
     */
    public AdminUserUpdateRequest(String email, String username, String countryCode, 
                                 String profileImageUrl, Role role, boolean active) {
        this.email = email;
        this.username = username;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.active = active;
    }

    // Getters and Setters

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

    public Role getRole() { 
        return role; 
    }
    
    public void setRole(Role role) { 
        this.role = role; 
    }

    public boolean isActive() { 
        return active; 
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }


    @Override
    public String toString() {
        return "AdminUserUpdateRequest{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
