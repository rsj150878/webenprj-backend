package at.fhtw.webenprjbackend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a registered user of the application.
 *
 * <p>Users have login credentials, profile information and a role
 * that defines their permissions.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Primary key of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email address used for login and communication.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Public username, also usable for login.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Hashed password (e.g. BCrypt).
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * ISO 3166-1 alpha-2 country code (e.g. "AT").
     */
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /**
     * URL of the user's profile image.
     */
    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    /**
     * Optional salutation or title (e.g., "Dr.", "Prof.", custom text).
     * Max 48 characters, used for personalized display.
     */
    @Column(name = "salutation", length = 48)
    private String salutation;

    /**
     * Role determining the user's permission level.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Flag indicating whether the account is active.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Timestamp when the user was created. Set once on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update of this user.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String email,
                String username,
                String password,
                String countryCode,
                String profileImageUrl,
                Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.active = true;
    }

    public User(String email,
                String username,
                String password,
                String countryCode,
                String profileImageUrl,
                String salutation,
                Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
        this.salutation = salutation;
        this.role = role;
        this.active = true;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

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

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
