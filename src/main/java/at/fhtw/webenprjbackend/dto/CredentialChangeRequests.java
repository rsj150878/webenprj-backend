package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Container for credential change request DTOs.
 * Groups related email and password change operations.
 */
public final class CredentialChangeRequests {

    private CredentialChangeRequests() {} // Utility class

    /**
     * Request DTO for changing user email.
     * Requires current password for security verification.
     */
    @Schema(description = "Request to change user email address with password verification")
    public static class EmailChange {

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

        public EmailChange() {}

        public EmailChange(String newEmail, String currentPassword) {
            this.newEmail = newEmail;
            this.currentPassword = currentPassword;
        }

        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    }

    /**
     * Request DTO for password change operations.
     * Requires the current password for security verification.
     */
    @Schema(description = "Password change request with current password verification")
    public static class PasswordChange {

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

        public PasswordChange() {}

        public PasswordChange(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

        /**
         * Validates that the new password is different from the current password
         */
        public boolean isPasswordChanged() {
            return currentPassword != null && newPassword != null && !currentPassword.equals(newPassword);
        }

        @Override
        public String toString() {
            return "PasswordChange{currentPassword='[PROTECTED]', newPassword='[PROTECTED]'}";
        }
    }
}
