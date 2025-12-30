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
import at.fhtw.webenprjbackend.security.ratelimit.RateLimitingFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final Environment environment;

    public SecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                                           JwtAuthenticationFilter jwtFilter,
                                           RateLimitingFilter rateLimitingFilter) throws Exception {

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
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/count").permitAll();

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

        // Add rate limiting filter first (before JWT auth)
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configureSecurityHeaders(HttpSecurity http, boolean isDevelopmentMode) throws Exception {
        http.headers(headers -> {
            if (isDevelopmentMode) {
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
            } else {
                headers.frameOptions(frameOptions -> frameOptions.deny());
            }

            headers.contentTypeOptions(org.springframework.security.config.Customizer.withDefaults());

            headers.xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));

            if (!isDevelopmentMode) {
                headers.httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)
                        .preload(true)
                );
            }

            headers.contentSecurityPolicy(csp -> csp.policyDirectives(cspDirectives(isDevelopmentMode)));

            headers.referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            );

            headers.cacheControl(cache -> cache.disable());
        });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        boolean isDevelopmentMode = isDevelopmentProfile();

        if (isDevelopmentMode) {
            config.setAllowedOrigins(List.of(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:5176",
                    "http://localhost:8080"
            ));
        } else {
            config.setAllowedOrigins(resolveProductionOrigins());
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private boolean isDevelopmentProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        // If no profile is active (default profile), treat as development
        if (activeProfiles.length == 0) {
            return true;
        }
        return Arrays.stream(activeProfiles)
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
