/**
 * Exception handling and error response structures for the API.
 *
 * <p>This package contains centralized components for mapping exceptions to
 * standardized error responses.
 *
 * <p>Main components:
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.exception.GlobalExceptionHandler}
 *       – global {@code @RestControllerAdvice} for REST exceptions</li>
 *   <li>{@link at.fhtw.webenprjbackend.exception.ErrorResponse}
 *       – DTO representing the API error response payload</li>
 *   <li>{@link at.fhtw.webenprjbackend.filestorage.FileException}
 *       – custom exception for file-related errors</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.exception.GlobalExceptionHandler
 * @see at.fhtw.webenprjbackend.exception.ErrorResponse
 * @see org.springframework.web.server.ResponseStatusException
 */
package at.fhtw.webenprjbackend.exception;
