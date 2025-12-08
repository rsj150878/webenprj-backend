package at.fhtw.webenprjbackend.service;

import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


/**
 * Custom UserDetailsService implementation for loading user-specific data during authentication.
 *
 * <p>This service bridges Spring Security's authentication mechanism with our User entity,
 * providing flexible login options and account status validation.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>Flexible Login:</b> Users can authenticate with either email address or username</li>
 *   <li><b>Account Status Check:</b> Validates that user accounts are active before authentication</li>
 *   <li><b>UserPrincipal Conversion:</b> Converts User entities to Spring Security UserDetails (UserPrincipal)</li>
 *   <li><b>Security Integration:</b> Automatically called by Spring Security's AuthenticationManager</li>
 * </ul>
 *
 * <p><b>Authentication Flow:</b>
 * <ol>
 *   <li>User submits login credentials (email/username + password) via /auth/login</li>
 *   <li>AuthenticationManager calls this service's loadUserByUsername() method</li>
 *   <li>We query database for user by email OR username</li>
 *   <li>Check if account is active (throw exception if disabled)</li>
 *   <li>Convert User entity to UserPrincipal (Spring Security's UserDetails)</li>
 *   <li>Return UserPrincipal to AuthenticationManager for password verification</li>
 *   <li>If password matches (checked by Spring Security), authentication succeeds</li>
 * </ol>
 *
 * <p><b>Design Decisions:</b>
 * <ul>
 *   <li><b>Email OR Username Login:</b> Provides user convenience while maintaining
 *       security (both are unique identifiers)</li>
 *   <li><b>Active Status Check:</b> Enables account suspension without deletion,
 *       throwing UsernameNotFoundException to prevent leaking account existence</li>
 *   <li><b>Exception Handling:</b> Uses generic "Invalid login or password" message
 *       to prevent user enumeration attacks</li>
 * </ul>
 *
 * <p><b>Security Note:</b> We intentionally use generic error messages to prevent
 * attackers from determining whether a username/email exists in the system.
 *
 * @see UserPrincipal
 * @see User
 * @see AuthService
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user-specific data for authentication by email or username.
     *
     * <p>This method is automatically called by Spring Security's AuthenticationManager
     * during the authentication process. It supports flexible login identifiers:
     * users can log in with either their email address or username.
     *
     * <p><b>Lookup Strategy:</b>
     * <ol>
     *   <li>First tries to find user by email</li>
     *   <li>If not found, tries to find user by username</li>
     *   <li>If neither exists, throws UsernameNotFoundException</li>
     * </ol>
     *
     * <p><b>Account Status Validation:</b>
     * <ul>
     *   <li>Checks if user account is active (user.isActive())</li>
     *   <li>Inactive accounts cannot log in (throws UsernameNotFoundException)</li>
     *   <li>This enables admin-controlled account suspension</li>
     * </ul>
     *
     * <p><b>Security:</b> Uses generic error messages ("Invalid login or password",
     * "User account is disabled") to prevent attackers from determining whether
     * an email/username exists in the system.
     *
     * @param login the user's login identifier (email OR username)
     * @return UserDetails (UserPrincipal) containing user data and authorities
     * @throws UsernameNotFoundException if user not found or account is disabled
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .or(() -> userRepository.findByUsername(login))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login or password"));

        // Check if user account is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        return UserPrincipal.fromUser(user);
    }

}
