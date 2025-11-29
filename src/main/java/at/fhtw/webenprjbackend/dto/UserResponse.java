package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response Data Transfer Object (DTO) for returning user information.
 * Contains all public user data without sensitive information (password, etc.).
 * Used across all user-related API responses in the Motivise study platform.
 */
@Schema(description = "User information returned in responses")
public class UserResponse {

    @Schema(
        description = "Unique user identifier", 
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final UUID id;
    
    @Schema(
        description = "User's email address", 
        example = "anna.schmidt@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String email;
    
    @Schema(
        description = "Unique username", 
        example = "study_anna",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String username;
    
    @Schema(
        description = "ISO 3166-1 alpha-2 country code", 
        example = "AT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String countryCode;
    
    @Schema(
        description = "Profile image URL (optional)", 
        example = "https://example.com/images/profile1.png",
        nullable = true
    )
    private final String profileImageUrl;
    
    @Schema(
        description = "User role determining access permissions", 
        example = "USER", 
        allowableValues = {"USER", "ADMIN"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String role;
    
    @Schema(
        description = "Account creation timestamp", 
        example = "2024-11-27T10:30:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final LocalDateTime createdAt;
    
    @Schema(
        description = "Last profile update timestamp", 
        example = "2024-11-27T15:45:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final LocalDateTime updatedAt;

    // ===============================
    // Constructor
    // ===============================

    /**
     * Creates a complete user response with all information
     * @param id Unique user identifier
     * @param email User's email address
     * @param username Unique username
     * @param countryCode ISO country code
     * @param profileImageUrl Optional profile image URL
     * @param role User role (USER or ADMIN)
     * @param createdAt Account creation timestamp
     * @param updatedAt Last update timestamp
     */
    public UserResponse(UUID id, String email, String username, String countryCode, 
                       String profileImageUrl, String role, LocalDateTime createdAt, 
                       LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ===============================
    // Getters
    // ===============================

    public UUID getId() { 
        return id; 
    }

    public String getEmail() { 
        return email; 
    }

    public String getUsername() { 
        return username; 
    }

    public String getCountryCode() { 
        return countryCode; 
    }

    public String getProfileImageUrl() { 
        return profileImageUrl; 
    }

    public String getRole() { 
        return role; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Checks if the user has a profile image
     * @return true if profileImageUrl is provided and not empty
     */
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    /**
     * Checks if the user is an admin
     * @return true if user role is ADMIN
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Checks if the profile was updated after creation
     * @return true if updatedAt is after createdAt
     */
    public boolean wasProfileUpdated() {
        return updatedAt != null && createdAt != null && 
               updatedAt.isAfter(createdAt);
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserResponse that = (UserResponse) obj;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
