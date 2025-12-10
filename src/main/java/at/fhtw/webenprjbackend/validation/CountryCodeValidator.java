package at.fhtw.webenprjbackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

/**
 * Validator for ISO 3166-1 alpha-2 country codes.
 * Maintains the official list of valid two-letter country codes.
 */
public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    private static final Set<String> VALID_CODES = Set.of(
        // Europe
        "AD", "AL", "AM", "AT", "AZ", "BA", "BE", "BG", "BY", "CH", "CY", "CZ", "DE", "DK", "EE",
        "ES", "FI", "FR", "GB", "GE", "GR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV",
        "MC", "MD", "ME", "MK", "MT", "NL", "NO", "PL", "PT", "RO", "RS", "RU", "SE", "SI", "SK",
        "SM", "TR", "UA", "VA", "XK",

        // Americas
        "AG", "AR", "BB", "BO", "BR", "BS", "BZ", "CA", "CL", "CO", "CR", "CU", "DM", "DO", "EC",
        "GD", "GT", "GY", "HN", "HT", "JM", "KN", "LC", "MX", "NI", "PA", "PE", "PY", "SR", "SV",
        "TT", "US", "UY", "VC", "VE",

        // Africa
        "AO", "BF", "BI", "BJ", "BW", "CD", "CF", "CG", "CI", "CM", "CV", "DJ", "DZ", "EG", "ER",
        "ET", "GA", "GH", "GM", "GN", "GQ", "GW", "KE", "KM", "LR", "LS", "LY", "MA", "MG", "ML",
        "MR", "MU", "MW", "MZ", "NA", "NE", "NG", "RW", "SC", "SD", "SL", "SN", "SO", "SS", "ST",
        "SZ", "TD", "TG", "TN", "TZ", "UG", "ZA", "ZM", "ZW",

        // Asia
        "AE", "AF", "BD", "BH", "BN", "BT", "CN", "ID", "IL", "IN", "IQ", "IR", "JO", "JP", "KG",
        "KH", "KP", "KR", "KW", "KZ", "LA", "LB", "LK", "MM", "MN", "MV", "MY", "NP", "OM", "PH",
        "PK", "PS", "QA", "SA", "SG", "SY", "TH", "TJ", "TL", "TM", "TW", "UZ", "VN", "YE",

        // Oceania
        "AU", "FJ", "FM", "KI", "MH", "NR", "NZ", "PG", "PW", "SB", "TO", "TV", "VU", "WS",

        // Caribbean & Other
        "AI", "AW", "BM", "CW", "FK", "GI", "GL", "GP", "GU", "KY", "MF", "MQ", "MS", "NC", "PF",
        "PM", "PR", "RE", "SX", "TC", "VG", "VI", "YT",

        // Special codes
        "AQ", "AS", "AX", "BL", "BQ", "CC", "CK", "CX", "EH", "FO", "GF", "GG", "HK", "HM", "IM",
        "IO", "JE", "MO", "MP", "NU", "PN", "SH", "SJ", "TF", "TK", "UM", "WF"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotBlank for null checks
        }
        return VALID_CODES.contains(value);
    }
}
