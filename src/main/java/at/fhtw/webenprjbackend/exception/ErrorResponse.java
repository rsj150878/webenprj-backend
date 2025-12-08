package at.fhtw.webenprjbackend.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response used for API exceptions.
 *
 * <p>Provides a consistent structure for error information returned by the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Short HTTP status description.
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Request path where the error occurred.
     */
    private String path;

    /**
     * Field-level validation errors (optional).
     */
    private Map<String, String> validationErrors;
}
