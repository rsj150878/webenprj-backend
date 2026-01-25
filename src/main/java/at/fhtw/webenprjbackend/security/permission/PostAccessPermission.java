package at.fhtw.webenprjbackend.security.permission;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostAccessPermission implements AccessPermission {

    private final PostRepository postRepository;

    @Override
    public boolean supports(Authentication authentication, String className) {
        return className.equals(Post.class.getName());
    }

    @Override
    public boolean hasPermission(Authentication authentication, UUID resourceId, String permission) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        Post post = postRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return post.getUser().getId().equals(principal.getId());
    }
}
