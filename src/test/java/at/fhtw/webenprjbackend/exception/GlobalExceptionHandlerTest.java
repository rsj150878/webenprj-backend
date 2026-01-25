package at.fhtw.webenprjbackend.exception;

import at.fhtw.webenprjbackend.security.ratelimit.RateLimitException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/test/path");
    }

    @Nested
    @DisplayName("handleResponseStatusException()")
    class ResponseStatusExceptionTests {

        @Test
        @DisplayName("should return 404 for NOT_FOUND exception")
        void notFoundException_returns404() {
            // Arrange
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
            assertThat(response.getBody().getError()).isEqualTo("Not Found");
            assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
            assertThat(response.getBody().getPath()).isEqualTo("/test/path");
        }

        @Test
        @DisplayName("should return 400 for BAD_REQUEST exception")
        void badRequestException_returns400() {
            // Arrange
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("should return 500 for server error exception")
        void serverErrorException_returns500() {
            // Arrange
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getStatus()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("handleValidationException()")
    class ValidationExceptionTests {

        @Test
        @DisplayName("should return 400 with field errors")
        void validationException_returns400WithFieldErrors() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            FieldError fieldError = new FieldError("user", "email", "Email is required");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid input data");
            assertThat(response.getBody().getValidationErrors()).containsEntry("email", "Email is required");
        }

        @Test
        @DisplayName("should include multiple field errors")
        void validationException_includesMultipleErrors() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            List<FieldError> fieldErrors = List.of(
                    new FieldError("user", "email", "Email is required"),
                    new FieldError("user", "password", "Password is too short")
            );
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, webRequest);

            // Assert
            assertThat(response.getBody().getValidationErrors()).hasSize(2);
            assertThat(response.getBody().getValidationErrors()).containsEntry("email", "Email is required");
            assertThat(response.getBody().getValidationErrors()).containsEntry("password", "Password is too short");
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation()")
    class ConstraintViolationTests {

        @Test
        @DisplayName("should return 400 with constraint message")
        void constraintViolation_returns400() {
            // Arrange
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("email");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("must be a valid email");

            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
            assertThat(response.getBody().getMessage()).contains("email");
            assertThat(response.getBody().getMessage()).contains("must be a valid email");
        }
    }

    @Nested
    @DisplayName("handleAccessDeniedException()")
    class AccessDeniedExceptionTests {

        @Test
        @DisplayName("should return 403 Forbidden")
        void accessDenied_returns403() {
            // Arrange
            AccessDeniedException ex = new AccessDeniedException("Access denied");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getStatus()).isEqualTo(403);
            assertThat(response.getBody().getError()).isEqualTo("Forbidden");
            assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to access this resource");
        }
    }

    @Nested
    @DisplayName("handleAuthenticationException()")
    class AuthenticationExceptionTests {

        @Test
        @DisplayName("should return 401 for bad credentials")
        void badCredentials_returns401WithSpecificMessage() {
            // Arrange
            BadCredentialsException ex = new BadCredentialsException("Bad credentials");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid email/username or password");
        }

        @Test
        @DisplayName("should return 401 for generic authentication failure")
        void genericAuthFailure_returns401WithGenericMessage() {
            // Arrange
            AuthenticationException ex = new AuthenticationException("Auth failed") {};

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
        }
    }

    @Nested
    @DisplayName("handleJwtException()")
    class JwtExceptionTests {

        @Test
        @DisplayName("should return 401 for JWT exception")
        void jwtException_returns401() {
            // Arrange
            JwtException ex = new JwtException("Token expired");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleJwtException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid or expired token");
        }
    }

    @Nested
    @DisplayName("handleRateLimitException()")
    class RateLimitExceptionTests {

        @Test
        @DisplayName("should return 429 with Retry-After header")
        void rateLimit_returns429WithHeader() {
            // Arrange
            RateLimitException ex = new RateLimitException(30);

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleRateLimitException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody().getError()).isEqualTo("Too Many Requests");
            assertThat(response.getBody().getMessage()).contains("30 seconds");
            assertThat(response.getHeaders().get("Retry-After")).contains("30");
        }
    }

    @Nested
    @DisplayName("handleGenericException()")
    class GenericExceptionTests {

        @Test
        @DisplayName("should return 500 for unexpected exceptions")
        void genericException_returns500() {
            // Arrange
            Exception ex = new RuntimeException("Unexpected error");

            // Act
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred.");
        }
    }
}
