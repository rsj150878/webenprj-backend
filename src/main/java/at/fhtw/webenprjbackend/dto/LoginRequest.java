package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login request DTO for user authentication.
 * Supports both email and username as login identifier.
 */
@Schema(description = "User login credentials")
public class LoginRequest {

    @NotBlank(message = "Login identifier is required")
    @Size(min = 3, max = 100, message = "Login must be between 3 and 100 characters")
    @Schema(
        description = "Email address or username for authentication (supports both formats)", 
        example = "anna.schmidt@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String login;   // can be email OR username

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    @Schema(
        description = "User password", 
        example = "Password123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    private String password;

    // ===============================
    // Constructors
    // ===============================
    
    /**
     * Default constructor for Jackson deserialization
     */
    public LoginRequest() {}

    /**
     * Constructor with all fields
     * @param login Email address or username
     * @param password User password
     */
    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // ===============================
    // Getters and Setters
    // ===============================
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Determines if the login identifier appears to be an email address
     * @return true if login contains @ symbol, false otherwise
     */
    public boolean isEmailLogin() {
        return login != null && login.contains("@");
    }

    // ===============================
    // Object Methods
    // ===============================
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "login='" + login + '\'' +
                ", password='[PROTECTED]'" +  // Don't log actual password
                '}';
    }
}
