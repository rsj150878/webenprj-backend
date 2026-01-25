package at.fhtw.webenprjbackend.controller;

import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.*;
import at.fhtw.webenprjbackend.security.jwt.JwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MediaController covering file upload/download operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MediaController Integration Tests")
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostBookmarkRepository postBookmarkRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtIssuer jwtIssuer;

    private User testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        postBookmarkRepository.deleteAll();
        postLikeRepository.deleteAll();
        followRepository.deleteAll();
        postRepository.deleteAll();
        mediaRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                "media@example.com",
                "mediauser",
                passwordEncoder.encode("Password123!"),
                "AT",
                "/avatar-placeholder.svg",
                Role.USER
        );
        testUser = userRepository.save(testUser);
        userToken = jwtIssuer.issue(testUser.getId(), testUser.getUsername(), "ROLE_USER");
    }

    @Nested
    @DisplayName("POST /medias")
    class UploadMediaTests {

        @Test
        @DisplayName("should return 201 when uploading valid image")
        void uploadMedia_validImage_returns201() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    "image/jpeg",
                    "fake image content".getBytes()
            );

            mockMvc.perform(multipart("/medias")
                            .file(file)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("test-image.jpg"));
        }

        @Test
        @DisplayName("should return 201 when uploading valid PDF")
        void uploadMedia_validPdf_returns201() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "fake pdf content".getBytes()
            );

            mockMvc.perform(multipart("/medias")
                            .file(file)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("document.pdf"));
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void uploadMedia_noAuth_returns403() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "content".getBytes()
            );

            mockMvc.perform(multipart("/medias")
                            .file(file))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 400 for invalid file type")
        void uploadMedia_invalidType_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "script.js",
                    "application/javascript",
                    "malicious script".getBytes()
            );

            mockMvc.perform(multipart("/medias")
                            .file(file)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /medias/{id}")
    class GetMediaTests {

        @Test
        @DisplayName("should return 404 for non-existent media")
        void getMedia_notFound_returns404() throws Exception {
            mockMvc.perform(get("/medias/" + UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should be accessible without authentication (public)")
        void getMedia_noAuthRequired() throws Exception {
            // First upload a file
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "public-image.jpg",
                    "image/jpeg",
                    "image content".getBytes()
            );

            String response = mockMvc.perform(multipart("/medias")
                            .file(file)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            // Extract ID from response and try to get without auth
            String mediaId = response.split("\"id\":\"")[1].split("\"")[0];

            mockMvc.perform(get("/medias/" + mediaId))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /medias/{id}")
    class DeleteMediaTests {

        @Test
        @DisplayName("should return 404 for non-existent media")
        void deleteMedia_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/medias/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 without authentication")
        void deleteMedia_noAuth_returns403() throws Exception {
            mockMvc.perform(delete("/medias/" + UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 204 when owner deletes media")
        void deleteMedia_asOwner_returns204() throws Exception {
            // First upload a file
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "to-delete.jpg",
                    "image/jpeg",
                    "content".getBytes()
            );

            String response = mockMvc.perform(multipart("/medias")
                            .file(file)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String mediaId = response.split("\"id\":\"")[1].split("\"")[0];

            // Delete the file
            mockMvc.perform(delete("/medias/" + mediaId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNoContent());
        }
    }
}
