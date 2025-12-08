package at.fhtw.webenprjbackend.security.permission;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.repository.MediaRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MediaAccessPermission implements AccessPermission {

    private final MediaRepository mediaRepository;

    @Override
    public boolean supports(Authentication authentication, String className) {
        return className.equals(Media.class.getName());
    }

    @Override
    public boolean hasPermission(Authentication authentication, UUID resourceId) {

        if (isAdmin(authentication)) {
            return true;
        }

        Media media = mediaRepository.findById(resourceId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        );

        return ((UserPrincipal) authentication.getPrincipal()).getId().equals(media.getCreateUser());
    }
}
