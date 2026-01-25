package at.fhtw.webenprjbackend.filestorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadValidatorTest {

    private FileUploadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileUploadValidator();
    }

    @Test
    void validate_shouldAcceptValidJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "dummy".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_shouldThrow400_whenFileIsNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow400_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                new byte[0]
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow413_whenFileIsTooLarge() {
        // > 25MB
        byte[] bigContent = new byte[(25 * 1024 * 1024) + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.png",
                "image/png",
                bigContent
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow400_whenFilenameIsBlank() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "   ",
                "image/png",
                "dummy".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow400_whenFilenameHasPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../evil.png",
                "image/png",
                "dummy".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow400_whenExtensionNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "dummy".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validate_shouldThrow400_whenMimeTypeNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/svg+xml",
                "dummy".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(file));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getAllowedFileTypesInfo_shouldContainAllowedTypesAndMaxSize() {
        String info = validator.getAllowedFileTypesInfo();

        assertNotNull(info);
        assertTrue(info.contains("Allowed file types"));
        assertTrue(info.contains("Maximum size"));
    }
}