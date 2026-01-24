package at.fhtw.webenprjbackend.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * OpenAPI configuration for Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Motivise API",
        version = "1.0.0",
        description = """
            REST API for the Motivise study micro-blogging platform.

            Most endpoints require JWT authentication. Login via `/auth/login` to get a token,
            then include it as `Authorization: Bearer {token}`.
            """
    )
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT token from /auth/login",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}