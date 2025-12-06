package at.fhtw.webenprjbackend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.fhtw.webenprjbackend.dto.UserResponse;
import at.fhtw.webenprjbackend.security.UserPrincipal;
import at.fhtw.webenprjbackend.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/users/{id}")
@Tag(name = "Follows", description = "Follow and unfollow users; list followers and following")
@Validated
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow")
    @Operation(summary = "Follow a user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Followed (idempotent)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> follow(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = ((UserPrincipal) authentication.getPrincipal()).getId();
        followService.follow(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow")
    @Operation(summary = "Unfollow a user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unfollowed (idempotent)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> unfollow(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = ((UserPrincipal) authentication.getPrincipal()).getId();
        followService.unfollow(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    @Operation(summary = "List followers of a user")
    public ResponseEntity<Page<UserResponse>> getFollowers(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(followService.getFollowers(id, pageable));
    }

    @GetMapping("/following")
    @Operation(summary = "List users followed by this user")
    public ResponseEntity<Page<UserResponse>> getFollowing(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(followService.getFollowing(id, pageable));
    }
}
