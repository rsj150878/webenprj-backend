package at.fhtw.webenprjbackend.security.permission;

import at.fhtw.webenprjbackend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.util.UUID;


public interface AccessPermission {
    Logger LOGGER = LoggerFactory.getLogger(AccessPermission.class);

    boolean supports(Authentication authentication, String className);
    boolean hasPermission(Authentication authentication, UUID resourceId);

    default  boolean isAdmin(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        LOGGER.debug("checking user: {}" , principal.getUsername());

        // Admin may access all posts
        return principal.hasRole("ADMIN");
    }

}
