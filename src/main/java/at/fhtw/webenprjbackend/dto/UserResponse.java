package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response Data Transfer Object (DTO) for returning user information.
 * Used to send user details back to the frontend without exposing sensitive data.
 *
 * Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.1
 */
public class UserResponse {

    private UUID id;
    private String email;
    private String username;
    private String countryCode;
    private String profileImageUrl;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    /**
     * Constructs a UserResponse with all relevant user information.
     */
    public UserResponse(UUID id, String email, String username, String countryCode, String profileImageUrl, String role, LocalDateTime createdAt, LocalDateTime updatedAt) {
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


}
