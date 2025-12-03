package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.dto.LoginRequest;
import at.fhtw.webenprjbackend.dto.LoginResponse;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;

import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Service layer for user authentication and JWT token management.
 *
 * <p>This service handles the complete authentication flow for the Motivise platform:
 * <ul>
 *   <li>Authenticating users via Spring Security's AuthenticationManager</li>
 *   <li>Generating JWT tokens using the TokenIssuer</li>
 *   <li>Setting the security context for the authenticated user</li>
 *   <li>Supporting login with both email and username (flexible identifier)</li>
 *   <li>Returning user profile information along with the JWT token</li>
 * </ul>
 *
 * <p><b>Architecture Decision: Why JWT instead of Sessions?</b>
 * <ul>
 *   <li><b>Horizontal Scaling:</b> Stateless authentication means no sticky sessions
 *       required - any backend server can validate any token without shared session storage</li>
 *   <li><b>Mobile/SPA Friendly:</b> Works seamlessly with single-page applications and
 *       mobile clients that don't handle cookies well</li>
 *   <li><b>Reduced Database Load:</b> No need to query a session table on every request
 *       - token validation happens in-memory using the secret key</li>
 *   <li><b>Microservices Ready:</b> Tokens can be validated by any service without
 *       centralized session store (future-proof architecture)</li>
 * </ul>
 *
 * <p><b>Trade-offs and Mitigations:</b>
 * <ul>
 *   <li><b>Token Revocation:</b> Cannot revoke tokens before expiration.
 *       Mitigation: Short expiry time (24 hours) + refresh token pattern for future implementation</li>
 *   <li><b>HTTP Header Size:</b> JWT tokens are larger than session IDs.
 *       Acceptable trade-off for our use case (~200 bytes per request)</li>
 * </ul>
 *
 * <p><b>Security Configuration:</b>
 * <ul>
 *   <li>Tokens expire after 24 hours (configurable via application.properties)</li>
 *   <li>Secret key is stored securely in environment variables</li>
 *   <li>Tokens are signed using HMAC-SHA256 for integrity verification</li>
 *   <li>Failed login attempts are logged for security monitoring</li>
 * </ul>
 *
 * @see TokenIssuer
 * @see CustomUserDetailsService
 * @see SecurityConfiguration
 * @see JwtAuthenticationFilter
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenIssuer tokenIssuer;
    private final UserRepository userRepository;

    /**
     * Authenticates a user and issues a JWT token.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Delegates authentication to Spring Security's AuthenticationManager</li>
     *   <li>Extracts the authenticated UserPrincipal from the authentication result</li>
     *   <li>Generates a signed JWT token containing user ID, username, and role</li>
     *   <li>Retrieves the full user profile from the database</li>
     *   <li>Returns both the JWT token and user profile information</li>
     * </ol>
     *
     * <p><b>Flexible Login Identifier:</b> Users can log in with either their email
     * address or username. The CustomUserDetailsService handles both cases automatically.
     *
     * @param request Login credentials containing login identifier (email or username) and password
     * @return LoginResponse containing the JWT token and complete user profile
     * @throws BadCredentialsException if the username/email or password is incorrect
     * @throws UsernameNotFoundException if the user account is disabled/inactive
     * @throws ResponseStatusException with UNAUTHORIZED status if user not found after authentication
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String role = principal.getAuthorities().iterator().next().getAuthority();
        String token = tokenIssuer.issue(principal.getId(), principal.getUsername(), role);

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token")
                );

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCountryCode(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return new LoginResponse(token, userResponse);
    }
}
