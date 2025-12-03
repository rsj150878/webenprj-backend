package at.fhtw.webenprjbackend.filestorage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Validates file uploads for security and correctness.
 * Prevents malicious file uploads, oversized files, and unsupported file types.
 */
@Component
public class FileUploadValidator {

    // Allowed file extensions (case-insensitive)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif",  // Images
            "pdf"                          // Documents
    );

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf"
    );

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    // Dangerous file extensions to explicitly block
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "jar", "war",  // Executables
            "js", "jsp", "php", "asp", "aspx",        // Server-side scripts
            "sql", "db", "mdb"                         // Database files
    );

    /**
     * Validates a file upload against security and size constraints.
     *
     * @param file the uploaded file to validate
     * @throws ResponseStatusException if validation fails
     */
    public void validate(MultipartFile file) {
        // Check if file is null or empty
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File cannot be empty"
            );
        }

        // Check file size
        validateFileSize(file);

        // Validate filename and extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid filename"
            );
        }

        validateFilename(originalFilename);
        validateFileExtension(originalFilename);

        // Validate MIME type
        validateMimeType(file.getContentType());
    }

    /**
     * Validates the file size is within allowed limits.
     */
    private void validateFileSize(MultipartFile file) {
        long fileSizeBytes = file.getSize();

        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            long fileSizeMB = fileSizeBytes / (1024 * 1024);
            long maxSizeMB = MAX_FILE_SIZE_BYTES / (1024 * 1024);
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    String.format("File size (%d MB) exceeds maximum allowed size (%d MB)",
                            fileSizeMB, maxSizeMB)
            );
        }

        if (fileSizeBytes == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File is empty (0 bytes)"
            );
        }
    }

    /**
     * Validates the filename for security issues.
     * Prevents path traversal attacks and dangerous characters.
     */
    private void validateFilename(String filename) {
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename contains invalid path characters"
            );
        }

        // Check for null bytes (potential security issue)
        if (filename.contains("\0")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename contains invalid null characters"
            );
        }

        // Check filename length
        if (filename.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename is too long (max 255 characters)"
            );
        }
    }

    /**
     * Validates the file extension is in the allowed list.
     */
    private void validateFileExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        // Check if extension is explicitly blocked
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("File type '.%s' is not allowed for security reasons", extension)
            );
        }

        // Check if extension is in allowed list
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("File type '.%s' is not supported. Allowed types: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }

    /**
     * Validates the MIME type is in the allowed list.
     */
    private void validateMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid content type"
            );
        }

        // Normalize MIME type (remove parameters like charset)
        String normalizedMimeType = mimeType.split(";")[0].trim().toLowerCase();

        if (!ALLOWED_MIME_TYPES.contains(normalizedMimeType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Content type '%s' is not supported. Allowed types: %s",
                            normalizedMimeType, String.join(", ", ALLOWED_MIME_TYPES))
            );
        }
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension (without the dot), or empty string if no extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Sanitizes a filename by removing or replacing potentially dangerous characters.
     * Returns a safe filename suitable for storage.
     *
     * @param originalFilename the original filename
     * @return sanitized filename
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) {
            return "file";
        }

        // Remove path components
        String filename = originalFilename.replaceAll("[/\\\\]", "");

        // Remove null bytes
        filename = filename.replace("\0", "");

        // Replace spaces and special characters with underscores
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Remove multiple consecutive underscores
        filename = filename.replaceAll("_{2,}", "_");

        // Ensure filename is not empty after sanitization
        if (filename.isBlank() || filename.equals("_")) {
            filename = "file";
        }

        return filename;
    }

    /**
     * Returns information about allowed file types.
     *
     * @return formatted string describing allowed file types
     */
    public String getAllowedFileTypesInfo() {
        return String.format("Allowed file types: %s. Maximum size: %d MB.",
                String.join(", ", ALLOWED_EXTENSIONS),
                MAX_FILE_SIZE_BYTES / (1024 * 1024));
    }
}
