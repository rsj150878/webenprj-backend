package at.fhtw.webenprjbackend.controller;

//TODO: For Milestone 2: DELETE UPDATE USER

import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


/**
 *  REST controller for managing user operations.
 *  Handles API requests related to user registration and retrieval.
 *
 *  Part of the Motivise study blogging platform backend.
 *
 * @author jasmin
 * @version 0.1
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor injection for UserService.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /users
     * Create a new user.
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /users
     * Retrieve all registered users.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /users/{id}
     * Retrieve a single user by UIID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
