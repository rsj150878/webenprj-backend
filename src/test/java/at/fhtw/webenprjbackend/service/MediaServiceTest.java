package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.filestorage.FileStorage;
import at.fhtw.webenprjbackend.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MediaService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MediaService")
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private MediaService mediaService;

    private UUID mediaId;
    private Media testMedia;

    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();
        testMedia = new Media();
        testMedia.setId(mediaId);
        testMedia.setName("test-image.jpg");
        testMedia.setExternalId("external-123");
        testMedia.setContentType("image/jpeg");
    }

    @Nested
    @DisplayName("upload()")
    class UploadTests {

        @Test
        @DisplayName("should upload file and save media entity")
        void upload_success() {
            // Arrange
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test-image.jpg");
            when(file.getContentType()).thenReturn("image/jpeg");
            when(fileStorage.upload(file)).thenReturn("external-123");
            when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
                Media media = invocation.getArgument(0);
                media.setId(mediaId);
                return media;
            });

            // Act
            Media result = mediaService.upload(file);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("test-image.jpg");
            assertThat(result.getExternalId()).isEqualTo("external-123");
            assertThat(result.getContentType()).isEqualTo("image/jpeg");

            verify(fileStorage).upload(file);
            verify(mediaRepository).save(any(Media.class));
        }

        @Test
        @DisplayName("should capture media entity with correct values")
        void upload_capturesCorrectValues() {
            // Arrange
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("document.pdf");
            when(file.getContentType()).thenReturn("application/pdf");
            when(fileStorage.upload(file)).thenReturn("pdf-external-id");
            when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);

            // Act
            mediaService.upload(file);

            // Assert
            verify(mediaRepository).save(mediaCaptor.capture());
            Media savedMedia = mediaCaptor.getValue();
            assertThat(savedMedia.getName()).isEqualTo("document.pdf");
            assertThat(savedMedia.getContentType()).isEqualTo("application/pdf");
            assertThat(savedMedia.getExternalId()).isEqualTo("pdf-external-id");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("should return media when found")
        void findById_found_returnsMedia() {
            // Arrange
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

            // Act
            Media result = mediaService.findById(mediaId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(mediaId);
            assertThat(result.getName()).isEqualTo("test-image.jpg");
        }

        @Test
        @DisplayName("should throw 404 when media not found")
        void findById_notFound_throws404() {
            // Arrange
            UUID unknownId = UUID.randomUUID();
            when(mediaRepository.findById(unknownId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> mediaService.findById(unknownId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Media not found");
        }
    }

    @Nested
    @DisplayName("asResource()")
    class AsResourceTests {

        @Test
        @DisplayName("should return resource from file storage")
        void asResource_returnsResource() {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("file content".getBytes());
            when(fileStorage.load(testMedia.getExternalId())).thenReturn(inputStream);

            // Act
            Resource result = mediaService.asResource(testMedia);

            // Assert
            assertThat(result).isNotNull();
            verify(fileStorage).load("external-123");
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete media and file")
        void delete_success() {
            // Arrange
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
            doNothing().when(fileStorage).delete(testMedia.getExternalId());
            doNothing().when(mediaRepository).delete(testMedia);

            // Act
            mediaService.delete(mediaId);

            // Assert
            verify(fileStorage).delete("external-123");
            verify(mediaRepository).delete(testMedia);
        }

        @Test
        @DisplayName("should throw 404 when media to delete not found")
        void delete_notFound_throws404() {
            // Arrange
            UUID unknownId = UUID.randomUUID();
            when(mediaRepository.findById(unknownId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> mediaService.delete(unknownId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Media not found");

            verify(fileStorage, never()).delete(anyString());
            verify(mediaRepository, never()).delete(any(Media.class));
        }
    }
}
