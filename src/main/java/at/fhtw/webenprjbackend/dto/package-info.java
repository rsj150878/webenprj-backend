/**
 * Data Transfer Objects (DTOs) for API request and response payloads.
 *
 * <p>This package contains all DTO classes used to transfer data between the client and server.
 * DTOs decouple the API contract from the domain model, providing validation, flexibility,
 * and security benefits.
 *
 * <p><b>Request DTOs:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.dto.LoginRequest}
 *       - User login credentials (email/username + password)</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.UserRegistrationRequest}
 *       - New user account creation data</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest}
 *       - User self-service profile updates</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.AdminUserUpdateRequest}
 *       - Admin user management updates (includes role changes)</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.ChangePasswordRequest}
 *       - Password change with current password verification</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.PostCreateRequest}
 *       - New study post creation data</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.PostUpdateRequest}
 *       - Existing post update data</li>
 * </ul>
 *
 * <p><b>Response DTOs:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.dto.LoginResponse}
 *       - JWT token + user profile after successful login</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.UserResponse}
 *       - User profile data (never includes password)</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.PostResponse}
 *       - Post data with author information</li>
 *   <li>{@link at.fhtw.webenprjbackend.dto.MediaDto}
 *       - File upload metadata</li>
 * </ul>
 *
 * <p><b>DTO Design Principles:</b>
 * <ul>
 *   <li><b>Validation:</b> All request DTOs use Jakarta Bean Validation annotations
 *       ({@code @NotBlank}, {@code @Email}, {@code @Size}, {@code @Pattern}, etc.)</li>
 *   <li><b>Immutability Preference:</b> Request DTOs are mutable for Jackson deserialization,
 *       but should be treated as immutable after creation</li>
 *   <li><b>No Business Logic:</b> DTOs are pure data holders with simple validation rules</li>
 *   <li><b>Security:</b> Sensitive fields (passwords) never included in response DTOs</li>
 *   <li><b>OpenAPI Annotations:</b> Comprehensive {@code @Schema} annotations for automatic
 *       API documentation in Swagger UI</li>
 * </ul>
 *
 * <p><b>Validation Patterns:</b>
 * <ul>
 *   <li><b>Email:</b> {@code @Email} + format validation</li>
 *   <li><b>Username:</b> {@code @Pattern} - letters, numbers, underscores only</li>
 *   <li><b>Password:</b> {@code @Pattern} - min 8 chars, uppercase, lowercase, digit required</li>
 *   <li><b>Country Code:</b> {@code @Pattern} - exactly 2 uppercase letters (ISO 3166-1 alpha-2)</li>
 *   <li><b>URLs:</b> {@code @Pattern} - HTTPS required, specific file extensions for images</li>
 * </ul>
 *
 * <p><b>Why DTOs Instead of Entities?</b>
 * <ul>
 *   <li><b>Security:</b> Prevents accidental exposure of sensitive fields (passwords, internal IDs)</li>
 *   <li><b>Validation:</b> Input validation rules differ from entity constraints</li>
 *   <li><b>Flexibility:</b> API can evolve independently of database schema</li>
 *   <li><b>Performance:</b> Can optimize payload size by including only needed fields</li>
 *   <li><b>Versioning:</b> Multiple DTO versions can coexist for API compatibility</li>
 *   <li><b>Documentation:</b> Clear API contracts with examples in Swagger UI</li>
 * </ul>
 *
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * @PostMapping("/users")
 * public ResponseEntity<UserResponse> registerUser(
 *     @Valid @RequestBody UserRegistrationRequest request
 * ) {
 *     UserResponse user = userService.registerUser(request);
 *     return ResponseEntity.status(HttpStatus.CREATED).body(user);
 * }
 * }</pre>
 *
 * <p><b>Utility Methods:</b>
 * <p>Many DTOs include utility methods for common checks:
 * <ul>
 *   <li>{@code hasProfileImage()} - Check if optional field is present</li>
 *   <li>{@code hasValidContent()} - Validate content quality beyond annotations</li>
 *   <li>{@code getHashtagSubject()} - Transform data for specific use cases</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.controller
 * @see at.fhtw.webenprjbackend.service
 * @see jakarta.validation
 */
package at.fhtw.webenprjbackend.dto;
