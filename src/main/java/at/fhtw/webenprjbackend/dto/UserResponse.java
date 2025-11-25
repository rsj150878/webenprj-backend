package at.fhtw.webenprjbackend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response Data Transfer Object (DTO) for returning user information.
 * Part of the Motivise study blogging platform backend.
 */
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String username;
    private final String countryCode;
    private final String profileImageUrl;
    private final String role;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
