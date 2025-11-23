package at.fhtw.webenprjbackend.dto;

public class LoginResponse {

    private String token;
    private UserResponse user;

    // ===============================
    // Constructor
    // ===============================
    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // ===============================
    // Getter
    // ===============================
    public String getToken() {
        return token;
    }
    public UserResponse getUser() {
        return user;
    }
}
