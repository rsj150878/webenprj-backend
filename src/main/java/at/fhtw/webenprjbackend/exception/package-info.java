/**
 * Exception handling and error response structures for the API.
 *
 * <p>This package provides centralized, consistent error handling across the entire application.
 * All exceptions are transformed into standardized error responses through the global exception handler.
 *
 * <p><b>Key Components:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.exception.GlobalExceptionHandler}
 *       - Central {@code @RestControllerAdvice} that intercepts all exceptions</li>
 *   <li>{@link at.fhtw.webenprjbackend.exception.ErrorResponse}
 *       - Standardized error response DTO for all API errors</li>
 *   <li>{@link at.fhtw.webenprjbackend.filestorage.FileException}
 *       - Custom exception for file upload/storage failures</li>
 * </ul>
 *
 * <p><b>Exception Handling Strategy:</b>
 * <ol>
 *   <li><b>ResponseStatusException:</b> Used throughout service layer for business logic errors
 *       <ul>
 *         <li>404 NOT_FOUND - Resource not found (User, Post, etc.)</li>
 *         <li>409 CONFLICT - Uniqueness violations (email/username already exists)</li>
 *         <li>400 BAD_REQUEST - Invalid input data</li>
 *         <li>413 PAYLOAD_TOO_LARGE - File size exceeds limit</li>
 *       </ul>
 *   </li>
 *   <li><b>MethodArgumentNotValidException:</b> Bean validation failures ({@code @Valid})</li>
 *   <li><b>AccessDeniedException:</b> Spring Security authorization failures</li>
 *   <li><b>AuthenticationException:</b> Spring Security authentication failures</li>
 *   <li><b>Exception:</b> Catch-all for unexpected errors (500 Internal Server Error)</li>
 * </ol>
 *
 * <p><b>Standardized Error Response Format:</b>
 * <pre>{@code
 * {
 *   "timestamp": "2024-12-05T10:30:00",
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "Email already exists: test@example.com",
 *   "path": "/users"
 * }
 * }</pre>
 *
 * <p>For validation errors, includes field-level details:
 * <pre>{@code
 * {
 *   "timestamp": "2024-12-05T10:30:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Invalid input data",
 *   "path": "/users",
 *   "validationErrors": {
 *     "email": "Email must be valid",
 *     "password": "Password must be at least 8 characters"
 *   }
 * }
 * }</pre>
 *
 * <p><b>Benefits of Centralized Exception Handling:</b>
 * <ul>
 *   <li><b>Consistency:</b> All errors follow the same response structure</li>
 *   <li><b>Logging:</b> Centralized error logging for monitoring and debugging</li>
 *   <li><b>Security:</b> Prevents internal exception details from leaking to clients</li>
 *   <li><b>Maintainability:</b> Single place to update error handling logic</li>
 *   <li><b>Frontend Integration:</b> Predictable error format simplifies client error handling</li>
 * </ul>
 *
 * <p><b>Logging Strategy:</b>
 * <ul>
 *   <li><b>ERROR level:</b> 5xx server errors (with full stack trace)</li>
 *   <li><b>WARN level:</b> 4xx client errors (message only)</li>
 *   <li><b>INFO level:</b> Validation failures</li>
 * </ul>
 *
 * <p><b>Example Service Usage:</b>
 * <pre>{@code
 * // Service throws ResponseStatusException
 * public UserResponse getUserById(UUID id) {
 *     User user = userRepository.findById(id)
 *         .orElseThrow(() -> new ResponseStatusException(
 *             HttpStatus.NOT_FOUND,
 *             "User not found with id: " + id
 *         ));
 *     return toResponse(user);
 * }
 *
 * // GlobalExceptionHandler automatically converts to ErrorResponse:
 * // {
 * //   "timestamp": "2024-12-05T10:30:00",
 * //   "status": 404,
 * //   "error": "Not Found",
 * //   "message": "User not found with id: 123e4567-e89b-12d3-a456-426614174000",
 * //   "path": "/users/123e4567-e89b-12d3-a456-426614174000"
 * // }
 * }</pre>
 *
 * <p><b>Why Not Custom Exception Classes?</b>
 * <p>Current design uses {@code ResponseStatusException} instead of custom exception
 * classes (e.g., {@code UserNotFoundException}, {@code EmailAlreadyExistsException}).
 * This is a pragmatic trade-off:
 *
 * <p><b>Advantages of ResponseStatusException:</b>
 * <ul>
 *   <li>Less code to maintain (no custom exception classes)</li>
 *   <li>Built-in Spring integration</li>
 *   <li>Quick to implement</li>
 * </ul>
 *
 * <p><b>Future Enhancement: Custom Domain Exceptions</b>
 * <p>For larger projects, consider custom exception classes for:
 * <ul>
 *   <li>Better semantic clarity (e.g., {@code EmailAlreadyExistsException})</li>
 *   <li>Carrying additional context (e.g., the email that was duplicate)</li>
 *   <li>Easier unit testing of specific error scenarios</li>
 *   <li>Error codes for programmatic frontend handling</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.exception.GlobalExceptionHandler
 * @see at.fhtw.webenprjbackend.exception.ErrorResponse
 * @see org.springframework.web.server.ResponseStatusException
 */
package at.fhtw.webenprjbackend.exception;
