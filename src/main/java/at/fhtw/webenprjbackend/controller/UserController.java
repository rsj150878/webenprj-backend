package at.fhtw.webenprjbackend.controller;

//TODO: For Milestone 2: DELETE UPDATE USER
//TODO: getAllUsers maybe alphabetic sorting?

import at.fhtw.webenprjbackend.dto.*;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 *  REST controller for managing user operations.
 *  Part of the Motivise study blogging platform backend.
 *
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========== Register (open, in security permitted) ==========
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========== Self-Service: own profile ==========
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getCurrentUser(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserResponse updated = userService.updateCurrentUserProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }


    // ========== Admin: User-List & Details ==========
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ========== Admin: Update User ==========
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminUpdateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        UserResponse updated = userService.adminUpdateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    // ========== Admin: Delete User ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteUser(@PathVariable UUID id) {
        userService.adminDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Admin: Block / Unblock User ==========
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminSetActive(
            @PathVariable UUID id,
            @RequestParam("active") boolean active) {

        UserResponse updated = userService.adminToggleActive(id, active);
        return ResponseEntity.ok(updated);
    }

    // ========== Admin: Search Users ==========
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam("q") String query) {
        return ResponseEntity.ok(userService.adminSearchUsers(query));
    }
}
