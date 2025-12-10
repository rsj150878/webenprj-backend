package at.fhtw.webenprjbackend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a country code is a valid ISO 3166-1 alpha-2 code.
 * Checks the value against the official list of 249 country codes.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CountryCodeValidator.class)
public @interface ValidCountryCode {
    String message() default "Invalid country code. Must be a valid ISO 3166-1 alpha-2 code (e.g., AT, DE, NL)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
