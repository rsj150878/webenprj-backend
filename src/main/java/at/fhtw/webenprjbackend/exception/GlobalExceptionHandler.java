package at.fhtw.webenprjbackend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for consistent error responses across the API.
 *
 * <p>This class intercepts all exceptions thrown by controllers and services,
 * transforming them into standardized {@link ErrorResponse} objects. This ensures:
 * <ul>
 *   <li>Consistent error format for frontend clients</li>
 *   <li>Centralized error logging for monitoring and debugging</li>
 *   <li>Proper HTTP status codes for all error scenarios</li>
 *   <li>Security by hiding internal exception details from clients</li>
 * </ul>
 *
 * <p><b>Exception Handling Strategy:</b>
 * <ol>
 *   <li><b>ResponseStatusException:</b> Most service-layer exceptions (404, 409, 400)</li>
 *   <li><b>MethodArgumentNotValidException:</b> Bean validation errors (@Valid)</li>
 *   <li><b>AccessDeniedException:</b> Spring Security authorization failures</li>
 *   <li><b>AuthenticationException:</b> Spring Security authentication failures</li>
 *   <li><b>Exception:</b> Catch-all for unexpected errors (500)</li>
 * </ol>
 *
 * <p><b>Logging Strategy:</b>
 * <ul>
 *   <li>5xx errors: ERROR level with full stack trace</li>
 *   <li>4xx errors: WARN level with message only</li>
 *   <li>Validation errors: INFO level</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResponseStatusException thrown by service layer.
     * This is the most common exception type in our codebase.
     *
     * <p>Examples:
     * <ul>
     *   <li>404 NOT_FOUND: User not found, Post not found</li>
     *   <li>409 CONFLICT: Email already exists, Username already exists</li>
     *   <li>400 BAD_REQUEST: Invalid password, Invalid file type</li>
     * </ul>
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            WebRequest request) {

        // Log based on severity
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        if (status.is5xxServerError()) {
            log.error("Server error [{}]: {} at {}",
                    status.value(),
                    ex.getReason(),
                    getPath(request),
                    ex);  // Include stack trace for 5xx
        } else if (status.is4xxClientError()) {
            log.warn("Client error [{}]: {} at {}",
                    status.value(),
                    ex.getReason(),
                    getPath(request));
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getReason())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Handles validation errors from @Valid annotations on request DTOs.
     *
     * <p>Aggregates all field-level validation errors into a single response
     * with a validationErrors map for easy frontend processing.
     *
     * <p>Example response:
     * <pre>
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Invalid input data",
     *   "validationErrors": {
     *     "email": "Email must be valid",
     *     "password": "Password must be at least 8 characters"
     *   }
     * }
     * </pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.info("Validation failed at {}: {} field errors",
                getPath(request),
                ex.getBindingResult().getErrorCount());

        // Build field error map
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid input data")
                .path(getPath(request))
                .validationErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles Spring Security access denied exceptions.
     *
     * <p>Occurs when an authenticated user tries to access a resource
     * they don't have permission for (e.g., non-admin accessing admin endpoints,
     * user trying to modify another user's post).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Access denied at {}: {}",
                getPath(request),
                ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("You do not have permission to access this resource")
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles Spring Security authentication failures.
     *
     * <p>Examples:
     * <ul>
     *   <li>Invalid username/password during login</li>
     *   <li>Expired JWT token</li>
     *   <li>Malformed JWT token</li>
     * </ul>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {

        log.warn("Authentication failed at {}: {}",
                getPath(request),
                ex.getMessage());

        // Use more specific message for bad credentials
        String message = ex instanceof BadCredentialsException
                ? "Invalid email/username or password"
                : "Authentication failed";

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(message)
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * <p>This prevents internal exception details from leaking to clients
     * while logging the full stack trace for debugging. Always returns
     * a generic 500 Internal Server Error response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected exception at {}: {}",
                getPath(request),
                ex.getMessage(),
                ex);  // Log full stack trace

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Extracts the request path from WebRequest for error logging and response.
     *
     * @param request the current web request
     * @return clean path (e.g., "/users/123") without "uri=" prefix
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
