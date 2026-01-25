package at.fhtw.webenprjbackend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DTO validation annotations.
 * Tests Jakarta Bean Validation constraints on request DTOs.
 */
@DisplayName("DTO Validation")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("CollectionCreateRequest")
    class CollectionCreateRequestTests {

        @Test
        @DisplayName("should accept valid request")
        void validRequest_noViolations() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "My Collection", "Description", "#3B82F6", "BookmarkIcon"
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject blank name")
        void blankName_shouldFail() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "   ", null, null, null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("should reject name exceeding 50 characters")
        void nameTooLong_shouldFail() {
            String longName = "A".repeat(51);
            CollectionCreateRequest request = new CollectionCreateRequest(
                    longName, null, null, null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("should accept valid hex color")
        void validHexColor_noViolations() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", null, "#FF5733", null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject invalid hex color - no hash")
        void invalidHexColor_noHash_shouldFail() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", null, "FF5733", null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("color"));
        }

        @Test
        @DisplayName("should reject invalid hex color - wrong length")
        void invalidHexColor_wrongLength_shouldFail() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", null, "#FFF", null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("color"));
        }

        @Test
        @DisplayName("should reject invalid hex color - invalid characters")
        void invalidHexColor_invalidChars_shouldFail() {
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", null, "#GGGGGG", null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("color"));
        }

        @Test
        @DisplayName("should reject description exceeding 200 characters")
        void descriptionTooLong_shouldFail() {
            String longDescription = "A".repeat(201);
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", longDescription, null, null
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
        }

        @Test
        @DisplayName("should reject icon name exceeding 50 characters")
        void iconNameTooLong_shouldFail() {
            String longIconName = "A".repeat(51);
            CollectionCreateRequest request = new CollectionCreateRequest(
                    "Test", null, null, longIconName
            );

            Set<ConstraintViolation<CollectionCreateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("iconName"));
        }
    }

    @Nested
    @DisplayName("PostUpdateRequest")
    class PostUpdateRequestTests {

        @Test
        @DisplayName("should accept valid request")
        void validRequest_noViolations() {
            PostUpdateRequest request = new PostUpdateRequest("#JavaTips", "This is valid content with more than ten characters.");

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should accept null fields (partial update)")
        void nullFields_noViolations() {
            PostUpdateRequest request = new PostUpdateRequest();

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject content shorter than 10 characters")
        void contentTooShort_shouldFail() {
            PostUpdateRequest request = new PostUpdateRequest(null, "Short");

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        }

        @Test
        @DisplayName("should reject content exceeding 500 characters")
        void contentTooLong_shouldFail() {
            String longContent = "A".repeat(501);
            PostUpdateRequest request = new PostUpdateRequest(null, longContent);

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        }

        @Test
        @DisplayName("should reject subject shorter than 2 characters")
        void subjectTooShort_shouldFail() {
            PostUpdateRequest request = new PostUpdateRequest("A", null);

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
        }

        @Test
        @DisplayName("should reject subject exceeding 30 characters")
        void subjectTooLong_shouldFail() {
            String longSubject = "A".repeat(31);
            PostUpdateRequest request = new PostUpdateRequest(longSubject, null);

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
        }

        @Test
        @DisplayName("should accept subject with hashtag prefix")
        void subjectWithHashtag_noViolations() {
            PostUpdateRequest request = new PostUpdateRequest("#SpringBoot", null);

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject subject with invalid characters")
        void subjectInvalidChars_shouldFail() {
            PostUpdateRequest request = new PostUpdateRequest("Test@Subject!", null);

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
        }

        @Test
        @DisplayName("should accept valid internal media URL")
        void validInternalMediaUrl_noViolations() {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setImageUrl("/medias/550e8400-e29b-41d4-a716-446655440000");

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should accept valid external image URL")
        void validExternalImageUrl_noViolations() {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setImageUrl("https://example.com/image.jpg");

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject invalid image URL")
        void invalidImageUrl_shouldFail() {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setImageUrl("not-a-valid-url");

            Set<ConstraintViolation<PostUpdateRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl"));
        }
    }

    @Nested
    @DisplayName("BookmarkRequest")
    class BookmarkRequestTests {

        @Test
        @DisplayName("should accept valid request with notes")
        void validRequest_noViolations() {
            BookmarkRequest request = new BookmarkRequest(null, "Great resource!");

            Set<ConstraintViolation<BookmarkRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should accept request with null notes")
        void nullNotes_noViolations() {
            BookmarkRequest request = new BookmarkRequest(null, null);

            Set<ConstraintViolation<BookmarkRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject notes exceeding 500 characters")
        void notesTooLong_shouldFail() {
            String longNotes = "A".repeat(501);
            BookmarkRequest request = new BookmarkRequest(null, longNotes);

            Set<ConstraintViolation<BookmarkRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("notes"));
        }

        @Test
        @DisplayName("should accept notes at maximum length")
        void notesAtMaxLength_noViolations() {
            String maxNotes = "A".repeat(500);
            BookmarkRequest request = new BookmarkRequest(null, maxNotes);

            Set<ConstraintViolation<BookmarkRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("CredentialChangeRequests.EmailChange")
    class EmailChangeTests {

        @Test
        @DisplayName("should accept valid email change request")
        void validRequest_noViolations() {
            CredentialChangeRequests.EmailChange request = new CredentialChangeRequests.EmailChange(
                    "newemail@example.com", "currentPassword123"
            );

            Set<ConstraintViolation<CredentialChangeRequests.EmailChange>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject blank email")
        void blankEmail_shouldFail() {
            CredentialChangeRequests.EmailChange request = new CredentialChangeRequests.EmailChange(
                    "   ", "currentPassword123"
            );

            Set<ConstraintViolation<CredentialChangeRequests.EmailChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newEmail"));
        }

        @Test
        @DisplayName("should reject invalid email format")
        void invalidEmailFormat_shouldFail() {
            CredentialChangeRequests.EmailChange request = new CredentialChangeRequests.EmailChange(
                    "not-an-email", "currentPassword123"
            );

            Set<ConstraintViolation<CredentialChangeRequests.EmailChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newEmail"));
        }

        @Test
        @DisplayName("should reject blank current password")
        void blankCurrentPassword_shouldFail() {
            CredentialChangeRequests.EmailChange request = new CredentialChangeRequests.EmailChange(
                    "newemail@example.com", "   "
            );

            Set<ConstraintViolation<CredentialChangeRequests.EmailChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
        }
    }

    @Nested
    @DisplayName("CredentialChangeRequests.PasswordChange")
    class PasswordChangeTests {

        @Test
        @DisplayName("should accept valid password change request")
        void validRequest_noViolations() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "NewPassword456"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject blank current password")
        void blankCurrentPassword_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "   ", "NewPassword456"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
        }

        @Test
        @DisplayName("should reject blank new password")
        void blankNewPassword_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "   "
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("should reject password shorter than 8 characters")
        void passwordTooShort_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "Short1"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("should reject password without uppercase letter")
        void passwordNoUppercase_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "lowercase123"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("should reject password without lowercase letter")
        void passwordNoLowercase_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "UPPERCASE123"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("should reject password without digit")
        void passwordNoDigit_shouldFail() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "currentPassword123", "NoDigitsHere"
            );

            Set<ConstraintViolation<CredentialChangeRequests.PasswordChange>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("isPasswordChanged should return true when passwords differ")
        void isPasswordChanged_differentPasswords_returnsTrue() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "oldPassword", "newPassword"
            );

            assertThat(request.isPasswordChanged()).isTrue();
        }

        @Test
        @DisplayName("isPasswordChanged should return false when passwords are same")
        void isPasswordChanged_samePasswords_returnsFalse() {
            CredentialChangeRequests.PasswordChange request = new CredentialChangeRequests.PasswordChange(
                    "samePassword", "samePassword"
            );

            assertThat(request.isPasswordChanged()).isFalse();
        }
    }

    @Nested
    @DisplayName("ChangePasswordRequest")
    class ChangePasswordRequestTests {

        @Test
        @DisplayName("should accept valid password change request")
        void validRequest_noViolations() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "currentPassword123", "NewPassword456"
            );

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should reject blank current password")
        void blankCurrentPassword_shouldFail() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "   ", "NewPassword456"
            );

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("should reject blank new password")
        void blankNewPassword_shouldFail() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "currentPassword123", "   "
            );

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("should reject password too short")
        void passwordTooShort_shouldFail() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "current", "Short1"
            );

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("should reject password without uppercase")
        void passwordNoUppercase_shouldFail() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "current", "lowercase123"
            );

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("isPasswordChanged should return true when different")
        void isPasswordChanged_different_returnsTrue() {
            ChangePasswordRequest request = new ChangePasswordRequest("old", "new");

            assertThat(request.isPasswordChanged()).isTrue();
        }

        @Test
        @DisplayName("isPasswordChanged should return false when same")
        void isPasswordChanged_same_returnsFalse() {
            ChangePasswordRequest request = new ChangePasswordRequest("same", "same");

            assertThat(request.isPasswordChanged()).isFalse();
        }

        @Test
        @DisplayName("toString should protect passwords")
        void toString_shouldProtectPasswords() {
            ChangePasswordRequest request = new ChangePasswordRequest("secret", "newsecret");

            String result = request.toString();

            assertThat(result).contains("[PROTECTED]");
            assertThat(result).doesNotContain("secret");
        }

        @Test
        @DisplayName("getters should work correctly")
        void getters_shouldWork() {
            ChangePasswordRequest request = new ChangePasswordRequest("current", "newpwd");

            assertThat(request.getCurrentPassword()).isEqualTo("current");
            assertThat(request.getNewPassword()).isEqualTo("newpwd");
        }

        @Test
        @DisplayName("setters should work correctly")
        void setters_shouldWork() {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("old");
            request.setNewPassword("new");

            assertThat(request.getCurrentPassword()).isEqualTo("old");
            assertThat(request.getNewPassword()).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("LoginResponse")
    class LoginResponseTests {

        @Test
        @DisplayName("should create valid response")
        void validResponse_shouldCreate() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("jwt.token.here", user);

            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("isValid should return true for valid response")
        void isValid_validData_returnsTrue() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("jwt.token.here", user);

            assertThat(response.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for null token")
        void isValid_nullToken_returnsFalse() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse(null, user);

            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("isValid should return false for empty token")
        void isValid_emptyToken_returnsFalse() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("   ", user);

            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("isValid should return false for null user")
        void isValid_nullUser_returnsFalse() {
            LoginResponse response = new LoginResponse("token", null);

            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("getUserRole should return role")
        void getUserRole_returnsRole() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "ADMIN", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("token", user);

            assertThat(response.getUserRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("getUserRole should return null for null user")
        void getUserRole_nullUser_returnsNull() {
            LoginResponse response = new LoginResponse("token", null);

            assertThat(response.getUserRole()).isNull();
        }

        @Test
        @DisplayName("getUserId should return id")
        void getUserId_returnsId() {
            java.util.UUID id = java.util.UUID.randomUUID();
            UserResponse user = new UserResponse(
                    id, "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("token", user);

            assertThat(response.getUserId()).isEqualTo(id);
        }

        @Test
        @DisplayName("getUserId should return null for null user")
        void getUserId_nullUser_returnsNull() {
            LoginResponse response = new LoginResponse("token", null);

            assertThat(response.getUserId()).isNull();
        }

        @Test
        @DisplayName("toString should truncate token")
        void toString_shouldTruncateToken() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("this.is.a.very.long.jwt.token.for.testing", user);

            String result = response.toString();

            assertThat(result).contains("...");
            assertThat(result).doesNotContain("for.testing");
        }

        @Test
        @DisplayName("equals and hashCode should work correctly")
        void equalsAndHashCode_shouldWork() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response1 = new LoginResponse("token", user);
            LoginResponse response2 = new LoginResponse("token", user);

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("equals should return false for different tokens")
        void equals_differentToken_returnsFalse() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response1 = new LoginResponse("token1", user);
            LoginResponse response2 = new LoginResponse("token2", user);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equals_null_returnsFalse() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("token", user);

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return true for same instance")
        void equals_sameInstance_returnsTrue() {
            UserResponse user = new UserResponse(
                    java.util.UUID.randomUUID(), "test@example.com", "testuser",
                    "AT", null, null, "USER", null, null, 0L, 0L
            );
            LoginResponse response = new LoginResponse("token", user);

            assertThat(response).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("LoginRequest")
    class LoginRequestTests {

        @Test
        @DisplayName("isEmailLogin should return true for email")
        void isEmailLogin_withEmail_returnsTrue() {
            LoginRequest request = new LoginRequest("test@example.com", "password");

            assertThat(request.isEmailLogin()).isTrue();
        }

        @Test
        @DisplayName("isEmailLogin should return false for username")
        void isEmailLogin_withUsername_returnsFalse() {
            LoginRequest request = new LoginRequest("username", "password");

            assertThat(request.isEmailLogin()).isFalse();
        }

        @Test
        @DisplayName("isEmailLogin should return false for null login")
        void isEmailLogin_nullLogin_returnsFalse() {
            LoginRequest request = new LoginRequest(null, "password");

            assertThat(request.isEmailLogin()).isFalse();
        }

        @Test
        @DisplayName("toString should protect password")
        void toString_shouldProtectPassword() {
            LoginRequest request = new LoginRequest("user", "secretpassword");

            String result = request.toString();

            assertThat(result).contains("[PROTECTED]");
            assertThat(result).doesNotContain("secretpassword");
        }

        @Test
        @DisplayName("setters should work correctly")
        void setters_shouldWork() {
            LoginRequest request = new LoginRequest();
            request.setLogin("user");
            request.setPassword("pass");

            assertThat(request.getLogin()).isEqualTo("user");
            assertThat(request.getPassword()).isEqualTo("pass");
        }
    }

    @Nested
    @DisplayName("UserRegistrationRequest")
    class UserRegistrationRequestTests {

        @Test
        @DisplayName("hasProfileImage should return true when set")
        void hasProfileImage_whenSet_returnsTrue() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "Password123!", "AT", "/medias/123e4567-e89b-12d3-a456-426614174000"
            );

            assertThat(request.hasProfileImage()).isTrue();
        }

        @Test
        @DisplayName("hasProfileImage should return false when null")
        void hasProfileImage_whenNull_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "Password123!", "AT"
            );

            assertThat(request.hasProfileImage()).isFalse();
        }

        @Test
        @DisplayName("hasProfileImage should return false when empty")
        void hasProfileImage_whenEmpty_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setProfileImageUrl("   ");

            assertThat(request.hasProfileImage()).isFalse();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return true when all set")
        void hasAllRequiredFields_allSet_returnsTrue() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "Password123!", "AT"
            );

            assertThat(request.hasAllRequiredFields()).isTrue();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return false when email missing")
        void hasAllRequiredFields_emailMissing_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    null, "testuser", "Password123!", "AT"
            );

            assertThat(request.hasAllRequiredFields()).isFalse();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return false when username empty")
        void hasAllRequiredFields_usernameEmpty_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "   ", "Password123!", "AT"
            );

            assertThat(request.hasAllRequiredFields()).isFalse();
        }

        @Test
        @DisplayName("isValidEmailFormat should return true for valid email")
        void isValidEmailFormat_valid_returnsTrue() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "Password123!", "AT"
            );

            assertThat(request.isValidEmailFormat()).isTrue();
        }

        @Test
        @DisplayName("isValidEmailFormat should return false for invalid email")
        void isValidEmailFormat_invalid_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "invalid", "testuser", "Password123!", "AT"
            );

            assertThat(request.isValidEmailFormat()).isFalse();
        }

        @Test
        @DisplayName("isValidUsernameFormat should return true for valid username")
        void isValidUsernameFormat_valid_returnsTrue() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "test_user123", "Password123!", "AT"
            );

            assertThat(request.isValidUsernameFormat()).isTrue();
        }

        @Test
        @DisplayName("isValidUsernameFormat should return false for invalid username")
        void isValidUsernameFormat_invalid_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "test@user!", "Password123!", "AT"
            );

            assertThat(request.isValidUsernameFormat()).isFalse();
        }

        @Test
        @DisplayName("isValidPasswordFormat should return true for valid password")
        void isValidPasswordFormat_valid_returnsTrue() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "Password123!", "AT"
            );

            assertThat(request.isValidPasswordFormat()).isTrue();
        }

        @Test
        @DisplayName("isValidPasswordFormat should return false for weak password")
        void isValidPasswordFormat_weak_returnsFalse() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "password", "AT"
            );

            assertThat(request.isValidPasswordFormat()).isFalse();
        }

        @Test
        @DisplayName("toString should protect password")
        void toString_shouldProtectPassword() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com", "testuser", "SecretPassword123!", "AT"
            );

            String result = request.toString();

            assertThat(result).contains("[PROTECTED]");
            assertThat(result).doesNotContain("SecretPassword123!");
        }

        @Test
        @DisplayName("all setters and getters should work")
        void settersAndGetters_shouldWork() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setEmail("new@email.com");
            request.setUsername("newuser");
            request.setPassword("NewPass123!");
            request.setCountryCode("DE");
            request.setProfileImageUrl("/medias/test");
            request.setSalutation("Dr.");

            assertThat(request.getEmail()).isEqualTo("new@email.com");
            assertThat(request.getUsername()).isEqualTo("newuser");
            assertThat(request.getPassword()).isEqualTo("NewPass123!");
            assertThat(request.getCountryCode()).isEqualTo("DE");
            assertThat(request.getProfileImageUrl()).isEqualTo("/medias/test");
            assertThat(request.getSalutation()).isEqualTo("Dr.");
        }
    }

    @Nested
    @DisplayName("PostCreateRequest")
    class PostCreateRequestTests {

        @Test
        @DisplayName("hasImage should return true when set")
        void hasImage_whenSet_returnsTrue() {
            PostCreateRequest request = new PostCreateRequest("#JavaTips", "Content here", "/medias/uuid");

            assertThat(request.hasImage()).isTrue();
        }

        @Test
        @DisplayName("hasImage should return false when null")
        void hasImage_whenNull_returnsFalse() {
            PostCreateRequest request = new PostCreateRequest("#JavaTips", "Content here");

            assertThat(request.hasImage()).isFalse();
        }

        @Test
        @DisplayName("hasImage should return false when empty")
        void hasImage_whenEmpty_returnsFalse() {
            PostCreateRequest request = new PostCreateRequest();
            request.setImageUrl("   ");

            assertThat(request.hasImage()).isFalse();
        }

        @Test
        @DisplayName("getHashtagSubject should add # if missing")
        void getHashtagSubject_noHash_addsHash() {
            PostCreateRequest request = new PostCreateRequest("JavaTips", "Content here");

            assertThat(request.getHashtagSubject()).isEqualTo("#JavaTips");
        }

        @Test
        @DisplayName("getHashtagSubject should not add # if already present")
        void getHashtagSubject_hasHash_noChange() {
            PostCreateRequest request = new PostCreateRequest("#JavaTips", "Content here");

            assertThat(request.getHashtagSubject()).isEqualTo("#JavaTips");
        }

        @Test
        @DisplayName("getHashtagSubject should return null if subject null")
        void getHashtagSubject_null_returnsNull() {
            PostCreateRequest request = new PostCreateRequest();

            assertThat(request.getHashtagSubject()).isNull();
        }

        @Test
        @DisplayName("hasValidContent should return true for valid content")
        void hasValidContent_valid_returnsTrue() {
            PostCreateRequest request = new PostCreateRequest("#JavaTips", "This is valid content");

            assertThat(request.hasValidContent()).isTrue();
        }

        @Test
        @DisplayName("hasValidContent should return false for short content")
        void hasValidContent_tooShort_returnsFalse() {
            PostCreateRequest request = new PostCreateRequest("#JavaTips", "ab");

            assertThat(request.hasValidContent()).isFalse();
        }

        @Test
        @DisplayName("hasValidContent should return false for null content")
        void hasValidContent_null_returnsFalse() {
            PostCreateRequest request = new PostCreateRequest();

            assertThat(request.hasValidContent()).isFalse();
        }

        @Test
        @DisplayName("toString should truncate long content")
        void toString_longContent_truncates() {
            String longContent = "A".repeat(100);
            PostCreateRequest request = new PostCreateRequest("#JavaTips", longContent);

            String result = request.toString();

            assertThat(result).contains("...");
        }

        @Test
        @DisplayName("all setters and getters should work")
        void settersAndGetters_shouldWork() {
            PostCreateRequest request = new PostCreateRequest();
            request.setSubject("#NewSubject");
            request.setContent("New content here");
            request.setImageUrl("/medias/test");
            request.setParentId(java.util.UUID.randomUUID());

            assertThat(request.getSubject()).isEqualTo("#NewSubject");
            assertThat(request.getContent()).isEqualTo("New content here");
            assertThat(request.getImageUrl()).isEqualTo("/medias/test");
            assertThat(request.getParentId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("UserProfileUpdateRequest")
    class UserProfileUpdateRequestTests {

        @Test
        @DisplayName("hasProfileImage should return true when set")
        void hasProfileImage_whenSet_returnsTrue() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT", "/medias/uuid"
            );

            assertThat(request.hasProfileImage()).isTrue();
        }

        @Test
        @DisplayName("hasProfileImage should return false when null")
        void hasProfileImage_whenNull_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT"
            );

            assertThat(request.hasProfileImage()).isFalse();
        }

        @Test
        @DisplayName("hasProfileImage should return false when empty")
        void hasProfileImage_whenEmpty_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setProfileImageUrl("   ");

            assertThat(request.hasProfileImage()).isFalse();
        }

        @Test
        @DisplayName("isValidEmailFormat should return true for valid email")
        void isValidEmailFormat_valid_returnsTrue() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT"
            );

            assertThat(request.isValidEmailFormat()).isTrue();
        }

        @Test
        @DisplayName("isValidEmailFormat should return false for invalid email")
        void isValidEmailFormat_invalid_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "invalid", "testuser", "AT"
            );

            assertThat(request.isValidEmailFormat()).isFalse();
        }

        @Test
        @DisplayName("isValidUsernameFormat should return true for valid username")
        void isValidUsernameFormat_valid_returnsTrue() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "test_user123", "AT"
            );

            assertThat(request.isValidUsernameFormat()).isTrue();
        }

        @Test
        @DisplayName("isValidUsernameFormat should return false for invalid username")
        void isValidUsernameFormat_invalid_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "test@user!", "AT"
            );

            assertThat(request.isValidUsernameFormat()).isFalse();
        }

        @Test
        @DisplayName("isValidCountryCode should return true for valid code")
        void isValidCountryCode_valid_returnsTrue() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT"
            );

            assertThat(request.isValidCountryCode()).isTrue();
        }

        @Test
        @DisplayName("isValidCountryCode should return false for lowercase")
        void isValidCountryCode_lowercase_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "at"
            );

            assertThat(request.isValidCountryCode()).isFalse();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return true when all set")
        void hasAllRequiredFields_allSet_returnsTrue() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT"
            );

            assertThat(request.hasAllRequiredFields()).isTrue();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return false when username empty")
        void hasAllRequiredFields_usernameEmpty_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "   ", "AT"
            );

            assertThat(request.hasAllRequiredFields()).isFalse();
        }

        @Test
        @DisplayName("hasAllRequiredFields should return false when countryCode null")
        void hasAllRequiredFields_countryNull_returnsFalse() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", null
            );

            assertThat(request.hasAllRequiredFields()).isFalse();
        }

        @Test
        @DisplayName("toString should include all fields")
        void toString_shouldIncludeFields() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    "test@example.com", "testuser", "AT", "/medias/uuid"
            );

            String result = request.toString();

            assertThat(result).contains("test@example.com");
            assertThat(result).contains("testuser");
            assertThat(result).contains("AT");
        }

        @Test
        @DisplayName("all setters and getters should work")
        void settersAndGetters_shouldWork() {
            UserProfileUpdateRequest request = new UserProfileUpdateRequest();
            request.setEmail("new@email.com");
            request.setUsername("newuser");
            request.setCountryCode("DE");
            request.setProfileImageUrl("/medias/test");
            request.setSalutation("Prof.");

            assertThat(request.getEmail()).isEqualTo("new@email.com");
            assertThat(request.getUsername()).isEqualTo("newuser");
            assertThat(request.getCountryCode()).isEqualTo("DE");
            assertThat(request.getProfileImageUrl()).isEqualTo("/medias/test");
            assertThat(request.getSalutation()).isEqualTo("Prof.");
        }
    }

    @Nested
    @DisplayName("AdminPostResponse")
    class AdminPostResponseTests {

        @Test
        @DisplayName("should create record with all fields")
        void shouldCreateRecord() {
            java.util.UUID id = java.util.UUID.randomUUID();
            java.util.UUID parentId = java.util.UUID.randomUUID();
            java.util.UUID userId = java.util.UUID.randomUUID();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            AdminPostResponse response = new AdminPostResponse(
                    id, parentId, 5L, false, "#Java", "Content", "/image.jpg",
                    now, now, userId, "username", "/avatar.jpg", 10L, true, 3L, false,
                    true, true, "user@example.com"
            );

            assertThat(response.id()).isEqualTo(id);
            assertThat(response.parentId()).isEqualTo(parentId);
            assertThat(response.commentCount()).isEqualTo(5L);
            assertThat(response.parentDeleted()).isFalse();
            assertThat(response.subject()).isEqualTo("#Java");
            assertThat(response.content()).isEqualTo("Content");
            assertThat(response.imageUrl()).isEqualTo("/image.jpg");
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.username()).isEqualTo("username");
            assertThat(response.likeCount()).isEqualTo(10L);
            assertThat(response.likedByCurrentUser()).isTrue();
            assertThat(response.bookmarkCount()).isEqualTo(3L);
            assertThat(response.bookmarkedByCurrentUser()).isFalse();
            assertThat(response.active()).isTrue();
            assertThat(response.isComment()).isTrue();
            assertThat(response.userEmail()).isEqualTo("user@example.com");
        }
    }
}
