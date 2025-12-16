package at.fhtw.webenprjbackend.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;

/**
 * Loads user data for authentication, supporting login by email or username.
 * Uses generic error messages to avoid leaking account existence.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .or(() -> userRepository.findByUsername(login))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login or password"));

        // Only active accounts may authenticate
        if (!user.isActive()) {
            throw new DisabledException("Your account has been deactivated. Please contact support.");
        }

        return UserPrincipal.fromUser(user);
    }

}
