package at.fhtw.webenprjbackend.security;

import java.util.Arrays;
import java.util.List;

import at.fhtw.webenprjbackend.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import at.fhtw.webenprjbackend.security.jwt.JwtAuthenticationFilter;
import at.fhtw.webenprjbackend.security.jwt.JwtDecoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final Environment environment;

    // Inject Environment to check active profiles
    public SecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // modern variant for AuthenticationManager (Spring Boot 3)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtDecoder jwtDecoder,
                                                           CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtDecoder, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {

        boolean isDevelopmentMode = isDevelopmentProfile();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> {
                    auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll();
                        
                    // Allow development tools in both dev and docker-free modes
                    if (isDevelopmentMode) {
                        auth
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll();
                    }
                    
                    auth.anyRequest().authenticated();
                });

        // Security Headers Configuration
        configureSecurityHeaders(http, isDevelopmentMode);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures comprehensive security headers for the application.
     * Different configurations for development vs production environments.
     *
     * @param http the HttpSecurity to configure
     * @param isDevelopmentMode whether the application is running in development mode
     */
    private void configureSecurityHeaders(HttpSecurity http, boolean isDevelopmentMode) throws Exception {
        http.headers(headers -> {
            // X-Frame-Options: Prevents clickjacking attacks
            if (isDevelopmentMode) {
                // Development: Allow same-origin frames (needed for H2 console)
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
            } else {
                // Production: Deny all framing
                headers.frameOptions(frameOptions -> frameOptions.deny());
            }

            // X-Content-Type-Options: Prevents MIME type sniffing
            // Forces browsers to respect the declared Content-Type
            headers.contentTypeOptions(org.springframework.security.config.Customizer.withDefaults());

            // X-XSS-Protection: Legacy XSS protection (mostly replaced by CSP)
            // Modern browsers use CSP, but this adds defense-in-depth
            headers.xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));

            // HTTP Strict Transport Security (HSTS)
            // Forces HTTPS connections for enhanced security
            if (!isDevelopmentMode) {
                headers.httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)           // Apply to all subdomains
                        .maxAgeInSeconds(31536000)          // 1 year
                        .preload(true)                      // Allow preload list submission
                );
            }
            // Note: HSTS disabled in development to allow HTTP testing

            // Content Security Policy (CSP)
            // Prevents XSS attacks by controlling resource loading
            headers.contentSecurityPolicy(csp -> csp.policyDirectives(cspDirectives(isDevelopmentMode)));

            // Referrer-Policy: Controls referrer information sent with requests
            // "strict-origin-when-cross-origin" balances privacy and functionality
            headers.referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            );

            // Cache-Control: Prevent sensitive data caching
            headers.cacheControl(cache -> cache.disable());
        });
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * Allows the frontend application to communicate with the backend API.
     *
     * Security Notes:
     * - allowCredentials=true allows cookies and Authorization headers
     * - Specific origins are whitelisted (no wildcards for security)
     * - Only necessary HTTP methods are allowed
     *
     * @return configured CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        boolean isDevelopmentMode = isDevelopmentProfile();

        if (isDevelopmentMode) {
            // Development: Allow common local development ports
            config.setAllowedOrigins(List.of(
                    "http://localhost:3000",      // React default
                    "http://localhost:5173",      // Vite default
                    "http://localhost:5176",      // Vite alternative port
                    "http://localhost:8080"       // Common alternative
            ));
        } else {
            // Production: Use environment variable for frontend URL
            // Set CORS_ALLOWED_ORIGINS in .env file
            config.setAllowedOrigins(resolveProductionOrigins());
        }

        // Allow necessary HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow necessary headers (Authorization for JWT, Content-Type for JSON)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Expose headers that frontend can read
        config.setExposedHeaders(List.of("Authorization"));

        // Allow credentials (cookies, authorization headers)
        // Required for JWT token authentication
        config.setAllowCredentials(true);

        // Cache preflight requests for 1 hour to reduce overhead
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private boolean isDevelopmentProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equals("dev") || profile.equals("docker-free"));
    }

    private List<String> resolveProductionOrigins() {
        String allowedOrigins = environment.getProperty("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            return List.of(allowedOrigins.split(","));
        }
        return List.of("https://motivise.app");
    }

    private String cspDirectives(boolean isDevelopmentMode) {
        if (isDevelopmentMode) {
            return String.join(" ",
                    "default-src 'self';",
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval';",
                    "style-src 'self' 'unsafe-inline';",
                    "img-src 'self' data: https:;",
                    "font-src 'self' data:;",
                    "frame-ancestors 'self'"
            );
        }
        return String.join(" ",
                "default-src 'self';",
                "script-src 'self';",
                "style-src 'self';",
                "img-src 'self' data: https:;",
                "font-src 'self';",
                "frame-ancestors 'none';",
                "base-uri 'self';",
                "form-action 'self'"
        );
    }
}
