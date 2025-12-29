package at.fhtw.webenprjbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO containing JWT token and user information after successful authentication.
 * Provides all necessary data for client-side authentication state management.
 * Used in the authentication flow of the Motivise study platform.
 */
@Schema(description = "Login response with JWT token and user details")
public class LoginResponse {

    @Schema(
        description = "JWT access token for API authentication", 
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbm5hLnNjaG1pZHRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDA2NTAwMDB9.signature",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String token;

    @Schema(
        description = "Authenticated user information without sensitive data", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final UserResponse user;

    // ===============================
    // Constructor
    // ===============================
    
    /**
     * Creates a login response with token and user data
     * @param token JWT access token for API authentication
     * @param user User information without sensitive data (password, etc.)
     */
    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // ===============================
    // Getters
    // ===============================
    
    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }

    // ===============================
    // Utility Methods
    // ===============================

    /**
     * Checks if the response contains valid authentication data
     * @return true if both token and user are present and valid
     */
    public boolean isValid() {
        return token != null && !token.trim().isEmpty() &&
               user != null && user.id() != null;
    }

    /**
     * Gets the user's role from the embedded user response
     * @return user role as string, or null if user data is missing
     */
    public String getUserRole() {
        return user != null ? user.role() : null;
    }

    /**
     * Gets the user's ID from the embedded user response
     * @return user ID, or null if user data is missing
     */
    public java.util.UUID getUserId() {
        return user != null ? user.id() : null;
    }

    // ===============================
    // Object Methods
    // ===============================
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + (token != null ? 
                    token.substring(0, Math.min(20, token.length())) + "..." : "null") + '\'' +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LoginResponse that = (LoginResponse) obj;
        return java.util.Objects.equals(token, that.token) && 
               java.util.Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(token, user);
    }
}
