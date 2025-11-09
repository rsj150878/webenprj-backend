package at.fhtw.webenprjbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// TODO: Bycrypt password before storing in DB??
// TODO: Add Salutation (Repeat Password)

/**
 * Data Transfer Object (DTO) for user registration.
 * Contains validation rules for creating new user accounts.
 *
 * Used in the POST /users endpoint of the Motivise platform.
 *
 * @author jasmin
 * @version 0.1
 */
public class UserRegistrationRequest {

    @NotBlank
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain upper, lower case letters and digits"
    )
    private String password;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be ISO-2 (e.g. AT, DE)")
    private String countryCode;


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
}
