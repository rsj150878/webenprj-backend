package at.fhtw.webenprjbackend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for all API errors.
 *
 * <p>This DTO provides a consistent structure for error responses across the entire API,
 * making it easier for frontend clients to handle errors predictably.
 *
 * <p><b>Example JSON Response:</b>
 * <pre>
 * {
 *   "timestamp": "2024-12-05T10:30:00",
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "Email already exists: test@example.com",
 *   "path": "/users"
 * }
 * </pre>
 *
 * <p>For validation errors, includes additional field-level error details:
 * <pre>
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
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't serialize null fields
public class ErrorResponse {

    /**
     * Timestamp when the error occurred (ISO-8601 format)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 400, 404, 409, 500)
     */
    private int status;

    /**
     * HTTP status text (e.g., "Bad Request", "Not Found", "Conflict")
     */
    private String error;

    /**
     * Human-readable error message describing what went wrong
     */
    private String message;

    /**
     * API endpoint path where the error occurred (e.g., "/users", "/posts/123")
     */
    private String path;

    /**
     * Field-level validation errors (only included for validation failures).
     * Maps field names to their respective error messages.
     *
     * <p>Example:
     * <pre>
     * {
     *   "email": "Email must be valid",
     *   "password": "Password must be at least 8 characters"
     * }
     * </pre>
     */
    private Map<String, String> validationErrors;
}
