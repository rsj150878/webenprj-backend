package at.fhtw.webenprjbackend.filestorage;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validates uploaded files for type, size and basic filename safety.
 */
@Component
public class FileUploadValidator {

    // Allowed file extensions (case-insensitive)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif",  // images
            "pdf"                         // documents
    );

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf"
    );

    // Maximum file size: 10 MB
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    // Explicitly blocked extensions
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "jar", "war",  // executables
            "js", "jsp", "php", "asp", "aspx",        // scripts
            "sql", "db", "mdb"                        // database files
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File cannot be empty"
            );
        }

        validateFileSize(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid filename"
            );
        }

        validateFilename(originalFilename);
        validateFileExtension(originalFilename);
        validateMimeType(file.getContentType());
    }

    private void validateFileSize(MultipartFile file) {
        long fileSizeBytes = file.getSize();

        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            long fileSizeMB = fileSizeBytes / (1024 * 1024);
            long maxSizeMB = MAX_FILE_SIZE_BYTES / (1024 * 1024);
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    String.format(
                            "File size (%d MB) exceeds maximum allowed size (%d MB)",
                            fileSizeMB, maxSizeMB
                    )
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
     * Rejects path traversal, null bytes and overly long filenames.
     */
    private void validateFilename(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename contains invalid path characters"
            );
        }

        if (filename.contains("\0")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename contains invalid characters"
            );
        }

        if (filename.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename is too long (max 255 characters)"
            );
        }
    }

    private void validateFileExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "File type '.%s' is not allowed for security reasons",
                            extension
                    )
            );
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "File type '.%s' is not supported. Allowed types: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS)
                    )
            );
        }
    }

    private void validateMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid content type"
            );
        }

        String normalizedMimeType = mimeType.split(";")[0].trim().toLowerCase();

        if (!ALLOWED_MIME_TYPES.contains(normalizedMimeType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "Content type '%s' is not supported. Allowed types: %s",
                            normalizedMimeType, String.join(", ", ALLOWED_MIME_TYPES)
                    )
            );
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Produces a filename safe for storage by removing risky characters.
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) {
            return "file";
        }

        String filename = originalFilename.replaceAll("[/\\\\]", "");
        filename = filename.replace("\0", "");
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        filename = filename.replaceAll("_{2,}", "_");

        if (filename.isBlank() || filename.equals("_")) {
            filename = "file";
        }

        return filename;
    }

    public String getAllowedFileTypesInfo() {
        return String.format(
                "Allowed file types: %s. Maximum size: %d MB.",
                String.join(", ", ALLOWED_EXTENSIONS),
                MAX_FILE_SIZE_BYTES / (1024 * 1024)
        );
    }
}
