package at.fhtw.webenprjbackend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class SecurityConfigurationTest {

    @Test
    void corsConfiguration_usesEnvOriginsInProd() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("CORS_ALLOWED_ORIGINS", "https://app.example.com,https://admin.example.com");

        SecurityConfiguration config = new SecurityConfiguration(env);
        CorsConfigurationSource source = config.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        var corsConfig = urlSource.getCorsConfiguration("/**");
        assertThat(corsConfig.getAllowedOrigins()).containsExactly(
                "https://app.example.com", "https://admin.example.com");
    }

    @Test
    void corsConfiguration_defaultsToLocalhostsInDev() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        SecurityConfiguration config = new SecurityConfiguration(env);
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();

        List<String> origins = urlSource.getCorsConfiguration("/**").getAllowedOrigins();
        assertThat(origins).contains("http://localhost:5173");
    }
}
