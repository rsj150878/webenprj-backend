package at.fhtw.webenprjbackend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.fhtw.webenprjbackend.dto.AdminUserUpdateRequest;
import at.fhtw.webenprjbackend.dto.ChangePasswordRequest;
import at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest;
import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for comprehensive user management in the Motivise platform.
 * Provides public registration, self-service profile management, and administrative user operations.
 * 
 * Features:
 * - Public user registration
 * - Self-service profile management (view, update, password change)
 * - Administrative user management (CRUD operations, search, activation)
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "Complete user lifecycle management - registration, profile updates, and admin operations")
public class UserController {

    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserService userService;

    /**
     * Constructor for dependency injection
     * @param userService Service layer for user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ===============================
    // Public Endpoints - Registration
    // ===============================

    @PostMapping
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with email, username, and password. Email and username must be unique. Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email or username already exists",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Conflict Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Email already exists: test@example.com\",\"path\":\"/users\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input data",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Email must be valid\",\"path\":\"/users\"}"
                )
            )
        )
    })
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===============================
    // Self-Service Endpoints - Profile Management
    // ===============================

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the profile information of the currently authenticated user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authentication Required",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"path\":\"/users/me\"}"
                )
            )
        )
    })
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getCurrentUser(principal.getId()));
    }

    @PutMapping("/me")
    @Operation(
        summary = "Update current user profile",
        description = "Update the profile information of the currently authenticated user (email, username, country, profile image).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Profile updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid update data",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Username must be between 3 and 50 characters\",\"path\":\"/users/me\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email or username already taken by another user",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Conflict Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Username already exists: new_username\",\"path\":\"/users/me\"}"
                )
            )
        )
    })
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserResponse updated = userService.updateCurrentUserProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/me/password")
    @Operation(
        summary = "Change password",
        description = "Change the password of the currently authenticated user. Requires current password for verification.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Password changed successfully"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid password data or current password incorrect",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Password Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Current password is incorrect\",\"path\":\"/users/me/password\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication required"
        )
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    // ===============================
    // Admin Endpoints - User Management
    // ===============================

    @GetMapping
    @Operation(
        summary = "Get all users (Admin only)",
        description = "Retrieve a list of all registered users with their profile information. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Users list retrieved successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "Authorization Error",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\",\"path\":\"/users\"}"
                )
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID (Admin only)",
        description = "Retrieve detailed information about a specific user by their ID. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User found and retrieved",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "User Not Found",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 123e4567-e89b-12d3-a456-426614174000\",\"path\":\"/users/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required"
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user (Admin only)",
        description = "Update any user's profile information including role changes. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid update data"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required"
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminUpdateUser(
            @Parameter(description = "User UUID to update", required = true) 
            @PathVariable UUID id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        UserResponse updated = userService.adminUpdateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user (Admin only)",
        description = "Permanently delete a user account and all associated data. This action cannot be undone. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                examples = @ExampleObject(
                    name = "User Not Found",
                    value = "{\"timestamp\":\"2024-11-27T15:30:00.123\",\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 123e4567-e89b-12d3-a456-426614174000\",\"path\":\"/users/123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required"
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteUser(
            @Parameter(description = "User UUID to delete", required = true) 
            @PathVariable UUID id) {
        userService.adminDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @Operation(
        summary = "Set user active status (Admin only)",
        description = "Activate or deactivate a user account. Deactivated users cannot login. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User status updated successfully",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required"
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminSetUserActive(
            @Parameter(description = "User UUID", required = true) 
            @PathVariable UUID id,
            @Parameter(description = "Set user active status", required = true, example = "true") 
            @RequestParam("active") boolean active) {

        UserResponse updated = userService.adminToggleActive(id, active);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search users (Admin only)",
        description = "Search users by email, username, or country code. Case-insensitive search. Requires admin privileges.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Search results retrieved (may be empty)",
            content = @Content(
                mediaType = MEDIA_TYPE_JSON,
                array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Admin privileges required"
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Search query to find users by email, username, or country", required = true, example = "anna") 
            @RequestParam("q") String query) {
        return ResponseEntity.ok(userService.adminSearchUsers(query));
    }
}
