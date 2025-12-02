package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import at.fhtw.webenprjbackend.dto.LoginRequest;
import at.fhtw.webenprjbackend.dto.LoginResponse;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name="Authentication", description="Endpoints for user authentication and JWT token management")
public class AuthController {

    private final AuthService authService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate with email OR username and password. Returns JWT token valid for API access."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful - JWT token and user info returned",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - invalid email/username or password",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Authentication Error",
                                    value = """
                           {
                             "timestamp":"2024-11-27T10:30:00.123+00:00",
                             "status":401,
                             "error":"Unauthorized",
                             "message":"Bad credentials",
                             "path":"/auth/login"
                           }
                           """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - missing or invalid input fields",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                           {
                             "timestamp":"2024-11-27T10:30:00.123+00:00",
                             "status":400,
                             "error":"Bad Request",
                             "message":"Login identifier is required",
                             "path":"/auth/login"
                           }
                           """
                            )
                    )
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        logger.info("Received login request {}", request.getLogin());
        LoginResponse response = authService.login(request);
        logger.info("Received login response {}", response);
        return ResponseEntity.ok(response);
    }


}
