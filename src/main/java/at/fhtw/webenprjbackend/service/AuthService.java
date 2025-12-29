package at.fhtw.webenprjbackend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.LoginRequest;
import at.fhtw.webenprjbackend.dto.LoginResponse;
import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.security.jwt.TokenIssuer;
import lombok.RequiredArgsConstructor;

/**
 * Service for authenticating users and issuing JWT tokens.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenIssuer tokenIssuer;
    private final UserRepository userRepository;

    /**
     * Authenticates a user and returns a token plus basic profile data.
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

        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
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
                user.getSalutation(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return new LoginResponse(token, userResponse);
    }
}
