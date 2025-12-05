package at.fhtw.webenprjbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import at.fhtw.webenprjbackend.dto.UserRegistrationRequest;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    private static final String DEFAULT_IMAGE = "https://static.example.com/default.png";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, followRepository, DEFAULT_IMAGE);

        when(passwordEncoder.encode("Password123!")).thenReturn("enc");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            try {
                var idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(u, UUID.randomUUID());
            } catch (Exception ignored) {
            }
            return u;
        });
    }

    @Test
    void registerUser_usesConfiguredDefaultProfileImage() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "anna@example.com",
                "anna",
                "Password123!",
                "AT",
                null
        );

        var response = userService.registerUser(request);

        assertThat(response.profileImageUrl()).isEqualTo(DEFAULT_IMAGE);
        assertThat(response.role()).isEqualTo(Role.USER.name());
    }

    @Test
    void updateUser_conflictingEmailThrowsConflict() {
        UUID existingId = UUID.randomUUID();
        User other = new User("other@example.com", "other", "pw", "AT", DEFAULT_IMAGE, Role.USER);
        when(userRepository.findById(existingId)).thenReturn(java.util.Optional.of(
                new User("anna@example.com", "anna", "pw", "AT", DEFAULT_IMAGE, Role.USER)
        ));
        when(userRepository.findByEmail("other@example.com")).thenReturn(java.util.Optional.of(other));

        assertThatThrownBy(() -> userService.updateCurrentUserProfile(
                existingId,
                new at.fhtw.webenprjbackend.dto.UserProfileUpdateRequest(
                        "other@example.com", "anna", "AT", DEFAULT_IMAGE
                )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode.value").isEqualTo(409);
    }
}
