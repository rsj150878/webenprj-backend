package at.fhtw.webenprjbackend.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing a registered user in the Motivise study blogging platform.
 *
 * <p>Users are the core participants in the Motivise community, capable of:
 * <ul>
 *   <li>Creating and sharing study posts</li>
 *   <li>Managing their own profile information</li>
 *   <li>Authenticating via email or username with password</li>
 *   <li>Having their accounts activated or deactivated by administrators</li>
 * </ul>
 *
 * <p><b>Database Schema:</b>
 * <ul>
 *   <li>Table: {@code users}</li>
 *   <li>Primary Key: {@code id} (UUID)</li>
 *   <li>Unique Constraints: {@code email}, {@code username}</li>
 *   <li>Indexes: Automatic on primary key and unique columns</li>
 * </ul>
 *
 * <p><b>Security Note:</b> The password field stores BCrypt-hashed passwords (never plaintext).
 * BCrypt is a one-way hash function with built-in salt, providing strong protection against
 * rainbow table attacks.
 *
 * @see Role
 * @see Post
 * @see UserService
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user (UUID v4).
     * Generated automatically upon user registration.
     * This ID is immutable and used for foreign key relationships across the system.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User's email address - used for login and notifications.
     * Must be unique across the platform (enforced by database constraint).
     * Validated against RFC 5322 email format at the DTO layer.
     *
     * <p>Email serves dual purposes:
     * <ul>
     *   <li>Authentication: Can be used as login identifier</li>
     *   <li>Communication: For password resets and notifications (future feature)</li>
     * </ul>
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Unique username for display and login purposes.
     * Must be unique across the platform (enforced by database constraint).
     * Limited to 50 characters, containing only letters, numbers, and underscores.
     *
     * <p>Usernames provide a memorable alternative to email for login and
     * are displayed publicly on posts and profiles.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * BCrypt-hashed password for authentication.
     * Never stores plaintext passwords - always hashed using BCrypt with 12 rounds.
     *
     * <p><b>Security:</b> BCrypt includes automatic salting and is computationally
     * expensive to slow down brute-force attacks. The hash includes the salt, algorithm
     * version, and cost factor in the stored value.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * ISO 3166-1 alpha-2 country code (e.g., "AT", "DE", "CH").
     * Used for localization, analytics, and potential regional features.
     *
     * <p>Must be exactly 2 uppercase letters. Validated at the DTO layer.
     */
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /**
     * URL pointing to the user's profile image.
     * Can be a user-uploaded image or the default placeholder.
     *
     * <p>If not provided during registration, defaults to a placeholder image URL.
     * URLs are validated for HTTPS and common image formats (jpg, png, gif, webp).
     */
    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    /**
     * User's role determining their permission level.
     * Stored as string in database for readability and future extensibility.
     *
     * <p>Current roles:
     * <ul>
     *   <li><b>USER:</b> Standard user with permission to manage their own posts and profile</li>
     *   <li><b>ADMIN:</b> Administrator with full access to all user and post management features</li>
     * </ul>
     *
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Account activation status flag.
     * When {@code false}, user cannot log in (account disabled by admin).
     * Used for account suspension without data deletion, allowing future reinstatement.
     *
     * <p>Defaults to {@code true} for new registrations.
     * Can only be changed by administrators via the admin API.
     *
     * <p><b>Design Rationale:</b> Soft deletion approach allows account recovery
     * and maintains data integrity (posts remain attributed to deactivated users).
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Timestamp of user account creation.
     * Automatically set by Hibernate on first persist - cannot be modified.
     * Used for analytics, debugging, and displaying account age.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last profile update.
     * Automatically updated by Hibernate on every save operation.
     * Tracks when user last modified their profile information.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // ===============================
    // Constructors
    // ===============================
    public User() {}

    public User(String email, String username, String password, String countryCode, String profileImageUrl, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.countryCode = countryCode;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.active = true;
    }

    // ===============================
    // Getters and Setters
    // ===============================
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
