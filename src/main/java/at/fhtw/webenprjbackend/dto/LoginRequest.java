package at.fhtw.webenprjbackend.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private String login;   // can be email OR username

    @NotBlank
    private String password;


    // ===============================
    // Getter + Setter
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
}
