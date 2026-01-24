package at.fhtw.webenprjbackend.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CountryCodeValidator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CountryCodeValidator")
class CountryCodeValidatorTest {

    private CountryCodeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CountryCodeValidator();
    }

    @Nested
    @DisplayName("isValid() - Valid Codes")
    class ValidCodesTests {

        @ParameterizedTest
        @ValueSource(strings = {"AT", "DE", "CH", "US", "GB", "FR", "JP", "AU", "NZ", "CA"})
        @DisplayName("should return true for common valid country codes")
        void commonCodes_valid(String code) {
            assertThat(validator.isValid(code, context)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"AD", "AL", "AM", "AZ", "BA", "BE", "BG", "BY"})
        @DisplayName("should return true for European country codes")
        void europeanCodes_valid(String code) {
            assertThat(validator.isValid(code, context)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"BR", "AR", "CL", "CO", "PE", "MX", "VE", "UY"})
        @DisplayName("should return true for South American country codes")
        void southAmericanCodes_valid(String code) {
            assertThat(validator.isValid(code, context)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"CN", "IN", "JP", "KR", "TH", "VN", "MY", "SG"})
        @DisplayName("should return true for Asian country codes")
        void asianCodes_valid(String code) {
            assertThat(validator.isValid(code, context)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ZA", "NG", "EG", "KE", "MA", "TN", "GH", "ET"})
        @DisplayName("should return true for African country codes")
        void africanCodes_valid(String code) {
            assertThat(validator.isValid(code, context)).isTrue();
        }

        @Test
        @DisplayName("should return true for null value (null checking is separate)")
        void nullValue_returnsTrue() {
            assertThat(validator.isValid(null, context)).isTrue();
        }
    }

    @Nested
    @DisplayName("isValid() - Invalid Codes")
    class InvalidCodesTests {

        @ParameterizedTest
        @ValueSource(strings = {"XX", "ZZ", "AA", "QQ", "YY"})
        @DisplayName("should return false for non-existent country codes")
        void nonExistentCodes_invalid(String code) {
            assertThat(validator.isValid(code, context)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"at", "de", "us", "gb"})
        @DisplayName("should return false for lowercase codes")
        void lowercaseCodes_invalid(String code) {
            assertThat(validator.isValid(code, context)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"A", "D", "1", "12"})
        @DisplayName("should return false for single character or numeric codes")
        void singleCharOrNumeric_invalid(String code) {
            assertThat(validator.isValid(code, context)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"USA", "GBR", "DEU", "AUT"})
        @DisplayName("should return false for three-letter codes (alpha-3)")
        void alpha3Codes_invalid(String code) {
            assertThat(validator.isValid(code, context)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("should return false for empty or whitespace codes")
        void emptyOrWhitespace_invalid(String code) {
            assertThat(validator.isValid(code, context)).isFalse();
        }

        @Test
        @DisplayName("should return false for mixed case codes")
        void mixedCase_invalid() {
            assertThat(validator.isValid("At", context)).isFalse();
            assertThat(validator.isValid("aT", context)).isFalse();
        }

        @Test
        @DisplayName("should return false for codes with spaces")
        void codesWithSpaces_invalid() {
            assertThat(validator.isValid("A T", context)).isFalse();
            assertThat(validator.isValid(" AT", context)).isFalse();
            assertThat(validator.isValid("AT ", context)).isFalse();
        }

        @Test
        @DisplayName("should return false for codes with special characters")
        void specialCharacters_invalid() {
            assertThat(validator.isValid("A-T", context)).isFalse();
            assertThat(validator.isValid("A.T", context)).isFalse();
            assertThat(validator.isValid("@#", context)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle Kosovo special code (XK)")
        void kosovoCode_valid() {
            // Kosovo uses XK as a user-assigned code
            assertThat(validator.isValid("XK", context)).isTrue();
        }

        @Test
        @DisplayName("should handle special territory codes")
        void specialTerritories_valid() {
            // Antarctica
            assertThat(validator.isValid("AQ", context)).isTrue();
            // Hong Kong
            assertThat(validator.isValid("HK", context)).isTrue();
            // Macau
            assertThat(validator.isValid("MO", context)).isTrue();
            // Taiwan
            assertThat(validator.isValid("TW", context)).isTrue();
        }

        @Test
        @DisplayName("should handle all DACH region codes")
        void dachRegionCodes_valid() {
            // Germany, Austria, Switzerland
            assertThat(validator.isValid("DE", context)).isTrue();
            assertThat(validator.isValid("AT", context)).isTrue();
            assertThat(validator.isValid("CH", context)).isTrue();
            // Liechtenstein (also German-speaking)
            assertThat(validator.isValid("LI", context)).isTrue();
        }
    }
}
