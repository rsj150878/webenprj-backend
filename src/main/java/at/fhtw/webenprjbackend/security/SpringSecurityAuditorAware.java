package at.fhtw.webenprjbackend.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("auditorAware")
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {

        System.out.println("in AUditorAware");
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of(UUID.randomUUID()); // Fallback
        }

        System.out.println("in AUditorAware " + ((UserPrincipal) authentication.getPrincipal()).getId());

        return Optional.of(((UserPrincipal) authentication.getPrincipal()).getId());
        // = Username aus dem JWT
    }
}
