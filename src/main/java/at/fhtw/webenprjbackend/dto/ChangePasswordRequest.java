package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for password change operations.
 * Requires the current password for security verification before allowing the password change.
 * Used in self-service user profile management.
 */
@Schema(description = "Password change request with current password verification")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(
        description = "User's current password for verification", 
        example = "Password123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(
        description = "New password meeting security requirements", 
        example = "NewPassword456!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    private String newPassword;

    // ===============================
    // Constructors
    // ===============================

    /**
     * Default constructor for Jackson deserialization
     */
    public ChangePasswordRequest() {}

    /**
     * Constructor with all fields
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     */
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public String getCurrentPassword() { 
        return currentPassword; 
    }
    
    public void setCurrentPassword(String currentPassword) { 
        this.currentPassword = currentPassword; 
    }

    public String getNewPassword() { 
        return newPassword; 
    }
    
    public void setNewPassword(String newPassword) { 
        this.newPassword = newPassword; 
    }

    // ===============================
    // Object Methods
    // ===============================

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }

    /**
     * Validates that the new password is different from the current password
     * @return true if passwords are different, false otherwise
     */
    public boolean isPasswordChanged() {
        return currentPassword != null && newPassword != null 
               && !currentPassword.equals(newPassword);
    }
}
