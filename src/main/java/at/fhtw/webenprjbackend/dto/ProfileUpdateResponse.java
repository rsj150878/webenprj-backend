package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for profile updates.
 * Includes the updated user data and optionally a new JWT token
 * when credentials (email/username) have changed.
 */
@Schema(description = "Response after updating user profile, includes new token if credentials changed")
public class ProfileUpdateResponse {

    @Schema(description = "Updated user information", required = true)
    private final UserResponse user;

    @Schema(
        description = "New JWT token - only present when email or username changed. " +
                      "Client should replace stored token with this new one.",
        nullable = true,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private final String token;

    @Schema(
        description = "Indicates whether credentials (email/username) were changed",
        example = "true"
    )
    private final boolean credentialsChanged;

    public ProfileUpdateResponse(UserResponse user, String token, boolean credentialsChanged) {
        this.user = user;
        this.token = token;
        this.credentialsChanged = credentialsChanged;
    }

    /**
     * Convenience constructor for when credentials haven't changed
     */
    public ProfileUpdateResponse(UserResponse user) {
        this(user, null, false);
    }

    public UserResponse getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public boolean isCredentialsChanged() {
        return credentialsChanged;
    }
}
