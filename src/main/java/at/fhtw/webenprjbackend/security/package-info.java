/**
 * Security infrastructure for authentication and authorization in the Motivise platform.
 *
 * <p>This package implements a comprehensive JWT-based authentication and authorization system
 * using Spring Security. It provides stateless authentication, role-based access control (RBAC),
 * and fine-grained resource-level permissions.
 *
 * <p><b>Core Components:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.security.SecurityConfiguration}
 *       - Main Spring Security configuration, filter chain, CORS, security headers</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.CustomUserDetailsService}
 *       - Loads user data for authentication, supports email OR username login</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.UserPrincipal}
 *       - Custom UserDetails implementation, adapts User entity to Spring Security</li>
 * </ul>
 *
 * <p><b>JWT Authentication:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.security.jwt.JwtIssuer}
 *       - Generates signed JWT tokens with user ID, username, and role</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.jwt.JwtDecoder}
 *       - Validates and parses JWT tokens, verifies signature</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.jwt.JwtAuthenticationFilter}
 *       - Servlet filter that intercepts requests, validates JWT, sets security context</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.jwt.JwtProperties}
 *       - Configuration properties for JWT secret key and expiration time</li>
 * </ul>
 *
 * <p><b>Permission System:</b>
 * <ul>
 *   <li>{@link at.fhtw.webenprjbackend.security.AccessPermissionEvaluator}
 *       - Custom PermissionEvaluator for fine-grained authorization</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.AccessPermission}
 *       - Interface for resource-specific permission handlers</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.PostAccessPermission}
 *       - Implements "user owns post OR is admin" logic for post operations</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.PermissionConfiguration}
 *       - Registers permission evaluator with Spring Security</li>
 * </ul>
 *
 * <p><b>Authentication Flow:</b>
 * <ol>
 *   <li>User sends credentials to {@code POST /auth/login}</li>
 *   <li>{@link at.fhtw.webenprjbackend.service.AuthService} calls AuthenticationManager</li>
 *   <li>AuthenticationManager delegates to {@link at.fhtw.webenprjbackend.security.CustomUserDetailsService}</li>
 *   <li>CustomUserDetailsService loads user from database, converts to {@link at.fhtw.webenprjbackend.security.UserPrincipal}</li>
 *   <li>AuthenticationManager verifies password using BCrypt</li>
 *   <li>On success, {@link at.fhtw.webenprjbackend.security.jwt.JwtIssuer} generates signed token</li>
 *   <li>Token returned to client with user profile data</li>
 * </ol>
 *
 * <p><b>Authorization Flow:</b>
 * <ol>
 *   <li>Client sends request with JWT in {@code Authorization: Bearer <token>} header</li>
 *   <li>{@link at.fhtw.webenprjbackend.security.jwt.JwtAuthenticationFilter} intercepts request</li>
 *   <li>Filter extracts token, validates signature using {@link at.fhtw.webenprjbackend.security.jwt.JwtDecoder}</li>
 *   <li>On valid token, loads user via CustomUserDetailsService</li>
 *   <li>Sets {@link org.springframework.security.core.context.SecurityContextHolder} with authenticated user</li>
 *   <li>Request proceeds to controller</li>
 *   <li>Spring Security checks {@code @PreAuthorize} annotations (if present)</li>
 *   <li>For custom permissions (e.g., {@code hasPermission()}), delegates to {@link at.fhtw.webenprjbackend.security.AccessPermissionEvaluator}</li>
 *   <li>If authorized, controller method executes; otherwise returns 403 Forbidden</li>
 * </ol>
 *
 * <p><b>Security Features:</b>
 * <ul>
 *   <li><b>Stateless Authentication:</b> No server-side session storage - all state in JWT token</li>
 *   <li><b>Token Expiration:</b> Tokens expire after 24 hours (configurable)</li>
 *   <li><b>BCrypt Password Hashing:</b> All passwords hashed with BCrypt (12 rounds by default)</li>
 *   <li><b>CORS Protection:</b> Strict origin whitelisting, credentials allowed</li>
 *   <li><b>Security Headers:</b> CSP, HSTS, X-Frame-Options, X-Content-Type-Options configured</li>
 *   <li><b>CSRF Protection:</b> Disabled (stateless JWT auth doesn't need CSRF tokens)</li>
 *   <li><b>User Enumeration Prevention:</b> Generic error messages for failed logins</li>
 * </ul>
 *
 * <p><b>Role-Based Access Control (RBAC):</b>
 * <ul>
 *   <li><b>USER Role:</b> Can manage own profile and posts</li>
 *   <li><b>ADMIN Role:</b> Full access to all resources, user management, system operations</li>
 *   <li>Roles stored in database, embedded in JWT token, checked via {@code @PreAuthorize("hasRole('ADMIN')")}</li>
 * </ul>
 *
 * <p><b>Resource-Level Permissions:</b>
 * <pre>{@code
 * @PreAuthorize("hasPermission(#id, 'at.fhtw.webenprjbackend.entity.Post', 'update')")
 * public ResponseEntity<PostResponse> updatePost(@PathVariable UUID id, ...) {
 *     // Only executes if:
 *     // - User owns the post (post.user_id == authenticated user ID), OR
 *     // - User has ADMIN role
 * }
 * }</pre>
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li><b>JWT Secret:</b> {@code app.jwt.secret} in application.properties (environment variable in production)</li>
 *   <li><b>JWT Expiration:</b> {@code app.jwt.expiration-ms} (default 24 hours)</li>
 *   <li><b>CORS Origins:</b> {@code CORS_ALLOWED_ORIGINS} environment variable for production</li>
 *   <li><b>Development Mode:</b> Auto-detected via {@code dev} or {@code docker-free} Spring profiles</li>
 * </ul>
 *
 * @see at.fhtw.webenprjbackend.service.AuthService
 * @see at.fhtw.webenprjbackend.entity.User
 * @see at.fhtw.webenprjbackend.controller.AuthController
 */
package at.fhtw.webenprjbackend.security;
