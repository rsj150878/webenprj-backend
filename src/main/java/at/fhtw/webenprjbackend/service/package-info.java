/**
 * Service layer containing business logic for the Motivise platform.
 *
 * <p>This package contains all service classes that implement business rules, orchestrate
 * operations, and coordinate between controllers and repositories. Services form the core
 * business logic layer, keeping controllers thin and focused on HTTP concerns.
 *
 * <p><b>Services:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.service.AuthService}
 *       - User authentication, JWT token generation, login flow</li>
 *   <li>{@link at.fhtw.webenprjbackend.service.UserService}
 *       - User registration, profile management, admin user operations</li>
 *   <li>{@link at.fhtw.webenprjbackend.service.PostService}
 *       - Study post CRUD, subject normalization, search functionality</li>
 *   <li>{@link at.fhtw.webenprjbackend.service.MediaService}
 *       - File upload handling, media storage coordination</li>
 * </ul>
 *
 * <p><b>Service Layer Responsibilities:</b>
 * <ul>
 *   <li><b>Business Logic:</b> Implement domain-specific rules (e.g., subject normalization,
 *       uniqueness validation, password hashing)</li>
 *   <li><b>Transaction Management:</b> Define transactional boundaries using {@code @Transactional}
 *       (inherits from Spring's defaults)</li>
 *   <li><b>Data Transformation:</b> Convert between entities and DTOs, prepare data for presentation</li>
 *   <li><b>Orchestration:</b> Coordinate multiple repository calls, external services, and complex workflows</li>
 *   <li><b>Validation:</b> Business-level validation beyond simple field constraints
 *       (e.g., uniqueness checks, password verification)</li>
 * </ul>
 *
 * <p><b>Design Principles:</b>
 * <ul>
 *   <li><b>Single Responsibility:</b> Each service focuses on one domain area
 *       (users, posts, authentication)</li>
 *   <li><b>Dependency Injection:</b> All dependencies injected via constructor for testability</li>
 *   <li><b>Exception Handling:</b> Services throw {@code ResponseStatusException} with appropriate
 *       HTTP status codes, caught and formatted by Spring's exception handlers</li>
 *   <li><b>No HTTP Concerns:</b> Services are HTTP-agnostic - they could be reused for
 *       other interfaces (CLI, GraphQL, etc.)</li>
 *   <li><b>Authorization-Free:</b> Authorization checks handled at controller layer via
 *       {@code @PreAuthorize} - services assume caller is authorized</li>
 * </ul>
 *
 * <p><b>Common Patterns:</b>
 * <ul>
 *   <li><b>Entity-DTO Mapping:</b> Services convert entities to response DTOs using
 *       {@code toResponse()} helper methods for consistency</li>
 *   <li><b>Uniqueness Validation:</b> Check repository for existing records before save,
 *       throw 409 Conflict if duplicate found</li>
 *   <li><b>Not Found Handling:</b> Use {@code orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))}
 *       for missing resources</li>
 *   <li><b>Password Security:</b> Always use {@code PasswordEncoder} for hashing - never
 *       store plaintext passwords</li>
 * </ul>
 *
 * <p><b>Transaction Management:</b>
 * <ul>
 *   <li>Services use Spring's default transactional behavior (read-write transactions)</li>
 *   <li>Each public service method runs in a transaction by default via {@code @Transactional}
 *       (when annotated at class or method level)</li>
 *   <li>Read-only operations should be annotated with {@code @Transactional(readOnly = true)}
 *       for performance optimization</li>
 *   <li>Transactions roll back automatically on unchecked exceptions (RuntimeException)</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b>
 * <pre>{@code
 * // Example: Not Found
 * User user = userRepository.findById(id)
 *     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 *
 * // Example: Conflict (uniqueness violation)
 * if (userRepository.existsByEmail(email)) {
 *     throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
 * }
 *
 * // Example: Bad Request (business rule violation)
 * if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
 *     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password incorrect");
 * }
 * }</pre>
 *
 * @see at.fhtw.webenprjbackend.controller
 * @see at.fhtw.webenprjbackend.repository
 * @see at.fhtw.webenprjbackend.entity
 * @see at.fhtw.webenprjbackend.dto
 */
package at.fhtw.webenprjbackend.service;
