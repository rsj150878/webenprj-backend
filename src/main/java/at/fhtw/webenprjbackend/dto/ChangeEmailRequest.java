package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for changing user email.
 * Requires current password for security verification.
 */
@Schema(description = "Request to change user email address with password verification")
public class ChangeEmailRequest {

    @NotBlank(message = "New email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(
        description = "New email address",
        example = "newemail@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newEmail;

    @NotBlank(message = "Current password is required")
    @Schema(
        description = "Current password for verification",
        example = "currentPassword123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currentPassword;

    // Constructors
    public ChangeEmailRequest() {}

    public ChangeEmailRequest(String newEmail, String currentPassword) {
        this.newEmail = newEmail;
        this.currentPassword = currentPassword;
    }

    // Getters and Setters
    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
