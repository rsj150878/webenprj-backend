/**
 * Security infrastructure for JWT-based authentication and authorization.
 * 
 * <p>Authentication flow: User credentials → AuthenticationManager → CustomUserDetailsService → 
 * BCrypt verification → JwtIssuer generates token → Token returned to client.
 * 
 * <p>Authorization flow: JWT in Authorization header → JwtAuthenticationFilter validates → 
 * SecurityContext set → @PreAuthorize checks roles/permissions → Controller executes or 403.
 * 
 * <p>Key features: Stateless JWT auth, BCrypt password hashing, role-based access control,
 * resource-level permissions (owner or admin), CORS protection, security headers.
 * 
 * @see at.fhtw.webenprjbackend.security.SecurityConfiguration
 * @see at.fhtw.webenprjbackend.security.jwt.JwtAuthenticationFilter
 * @see at.fhtw.webenprjbackend.security.permission.AccessPermissionEvaluator
 */
package at.fhtw.webenprjbackend.security;
