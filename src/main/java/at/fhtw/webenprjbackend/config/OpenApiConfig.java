package at.fhtw.webenprjbackend.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Central OpenAPI 3.0 configuration for the Motivise Study Platform API.
 * Defines global API documentation settings, security schemes, and server information.
 * 
 * This configuration provides:
 * - API metadata (title, version, description)
 * - JWT Bearer token authentication scheme
 * - Development and production server definitions
 * - Contact and license information
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Motivise Study Platform API",
        version = "1.0.0",
        description = """
            **Complete REST API for the Motivise study micro-blogging platform.**
            
            ## Features:
            - 🔐 **JWT Authentication** - Secure user authentication with Bearer tokens
            - 👤 **User Management** - Registration, profile management, and admin operations  
            - 📚 **Study Posts** - Create, read, update, and delete study progress posts
            - 🔍 **Search** - Find users and posts by keywords
            - 🌍 **International** - Multi-country support with ISO country codes
            - 🖼️ **Media Support** - Profile images and post attachments
            
            ## Authentication:
            Most endpoints require JWT authentication. Use the `/auth/login` endpoint to obtain a token,
            then include it in the `Authorization` header as `Bearer {token}`.
            
            ## Getting Started:
            1. Register a new user account with `POST /users`
            2. Login to get JWT token with `POST /auth/login`  
            3. Create your first study post with `POST /posts`
            4. Explore other users and posts with the search endpoints
            """
    ),
    servers = {
        @Server(
            url = "http://localhost:8081",
            description = "🛠️ Development Server - Local development environment"
        ),
        @Server(
            url = "https://api.motivise.app",
            description = "🚀 Production Server - Live production environment"
        ),
        @Server(
            url = "https://staging.api.motivise.app",
            description = "🧪 Staging Server - Pre-production testing environment"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = """
        JWT Bearer token authentication. 
        
        **How to use:**
        1. Login via `POST /auth/login` to get your JWT token
        2. Copy the `token` value from the response
        3. Click the 🔒 **Authorize** button above
        4. Enter: `Bearer {your-token-here}`
        5. All authenticated endpoints will now work automatically!
        
        **Token format:** `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
        """,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
    
    // This class doesn't need any methods - the annotations do all the work!
    // Spring Boot will automatically pick up these configurations.
}