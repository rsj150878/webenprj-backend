package at.fhtw.webenprjbackend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostCreateRequest DTO using JUnit 5
 * Tests validation logic, constructors, and utility methods
 */
@DisplayName("PostCreateRequest DTO Tests")
class PostCreateRequestTest {

    private static PostCreateRequest validRequest;
    private static PostCreateRequest requestWithImage;
    private static PostCreateRequest requestWithoutImage;

    @BeforeAll
    static void setUpTestData() {
        validRequest = new PostCreateRequest("#JavaLearning", "Just finished learning Spring Boot dependency injection!", "https://example.com/spring-diagram.png");
        requestWithImage = new PostCreateRequest("#Test", "Content with image", "https://example.com/image.png");
        requestWithoutImage = new PostCreateRequest("#Test", "Content without image", null);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create valid post request with all parameters")
        void constructor_WhenAllParametersProvided_ShouldCreateValidRequest() {
            // Given
            String subject = "#JavaLearning";
            String content = "Just finished learning Spring Boot dependency injection!";
            String imageUrl = "https://example.com/spring-diagram.png";
            
            // When
            PostCreateRequest request = new PostCreateRequest(subject, content, imageUrl);
            
            // Then
            assertEquals(subject, request.getSubject());
            assertEquals(content, request.getContent());
            assertEquals(imageUrl, request.getImageUrl());
        }

        @Test
        @DisplayName("Should handle default constructor")
        void constructor_WhenDefaultConstructor_ShouldCreateEmptyRequest() {
            // When
            PostCreateRequest defaultRequest = new PostCreateRequest();
            
            // Then
            assertNull(defaultRequest.getSubject());
            assertNull(defaultRequest.getContent());
            assertNull(defaultRequest.getImageUrl());
        }

        @Test
        @DisplayName("Should handle constructor without image")
        void constructor_WhenNoImageProvided_ShouldCreateRequestWithoutImage() {
            // When
            PostCreateRequest noImageRequest = new PostCreateRequest("#Test", "Content");
            
            // Then
            assertEquals("#Test", noImageRequest.getSubject());
            assertEquals("Content", noImageRequest.getContent());
            assertNull(noImageRequest.getImageUrl());
        }
    }

    @Nested
    @DisplayName("Image Detection Tests")
    class ImageDetectionTests {

        @Test
        @DisplayName("Should detect when image is present")
        void hasImage_WhenImageUrlProvided_ShouldReturnTrue() {
            // Then
            assertTrue(requestWithImage.hasImage());
        }

        @Test
        @DisplayName("Should detect when image is not present")
        void hasImage_WhenImageUrlIsNull_ShouldReturnFalse() {
            // Then
            assertFalse(requestWithoutImage.hasImage());
        }

        @Test
        @DisplayName("Should detect when image URL is empty")
        void hasImage_WhenImageUrlIsEmpty_ShouldReturnFalse() {
            // Given
            PostCreateRequest emptyImageRequest = new PostCreateRequest("#Test", "Content with empty image", "");
            
            // Then
            assertFalse(emptyImageRequest.hasImage());
        }
    }

    @Nested
    @DisplayName("Subject Formatting Tests")
    class SubjectFormattingTests {

        @Test
        @DisplayName("Should format subject with hashtag when already present")
        void getHashtagSubject_WhenSubjectHasHashtag_ShouldReturnAsIs() {
            // Given
            PostCreateRequest withHash = new PostCreateRequest("#JavaLearning", "Learning content");
            
            // Then
            assertEquals("#JavaLearning", withHash.getHashtagSubject());
        }

        @Test
        @DisplayName("Should add hashtag when not present")
        void getHashtagSubject_WhenSubjectMissingHashtag_ShouldAddHashtag() {
            // Given
            PostCreateRequest withoutHash = new PostCreateRequest("JavaLearning", "Learning content");
            
            // Then
            assertEquals("#JavaLearning", withoutHash.getHashtagSubject());
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @DisplayName("Should validate content with sufficient length")
        void hasValidContent_WhenContentIsLongEnough_ShouldReturnTrue() {
            // Given
            PostCreateRequest validContent = new PostCreateRequest("#Test", "This is a valid content with more than 10 characters");
            
            // Then
            assertTrue(validContent.hasValidContent());
        }

        @Test
        @DisplayName("Should invalidate short content")
        void hasValidContent_WhenContentIsTooShort_ShouldReturnFalse() {
            // Given
            PostCreateRequest shortContent = new PostCreateRequest("#Test", "Short");
            
            // Then
            assertFalse(shortContent.hasValidContent());
        }

        @Test
        @DisplayName("Should invalidate null content")
        void hasValidContent_WhenContentIsNull_ShouldReturnFalse() {
            // Given
            PostCreateRequest nullContent = new PostCreateRequest("#Test", null);
            
            // Then
            assertFalse(nullContent.hasValidContent());
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should handle toString method correctly")
        void toString_WhenCalled_ShouldReturnValidStringRepresentation() {
            // Given
            PostCreateRequest request = new PostCreateRequest("#Test", "This is a very long content that should be truncated", "https://example.com/image.png");
            
            // When
            String result = request.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("#Test"));
            assertTrue(result.contains("PostCreateRequest"));
        }
    }
}
