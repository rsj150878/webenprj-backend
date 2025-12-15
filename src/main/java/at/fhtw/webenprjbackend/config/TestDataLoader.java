package at.fhtw.webenprjbackend.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.service.MediaService;

/**
 * Configurable test data loader.
 * Controlled by app.data.load-dev-data property.
 */
@Configuration
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);

    @Bean
    @ConditionalOnProperty(name = "app.data.load-dev-data", havingValue = "true")
    CommandLineRunner initDatabase(UserRepository userRepository,
                                 PostRepository postRepository,
                                 PasswordEncoder passwordEncoder,
                                 MediaService mediaService) {
        return args -> {
            // Check if data already exists (prevent duplicates)
            if (userRepository.count() > 0) {
                log.info("Database already contains data, skipping test data load");
                return;
            }
            
            log.info("Loading fresh test data for development...");

            // Upload avatars
            log.info("Uploading test user avatars...");
            String annaAvatarUrl = uploadAvatarFromResources(
                "test-data/avatars/anna-avatar.avif", mediaService);
            String maxAvatarUrl = uploadAvatarFromResources(
                "test-data/avatars/max-avatar.avif", mediaService);
            String adminAvatarUrl = uploadAvatarFromResources(
                "test-data/avatars/admin-avatar.avif", mediaService);

            // Create test users with avatar URLs
            User anna = createUser("anna.schmidt@example.com", "study_anna",
                "Password123!", "AT", Role.USER, annaAvatarUrl, passwordEncoder);
            User max = createUser("max.meier@example.com", "maxlearns",
                "Password123!", "DE", Role.USER, maxAvatarUrl, passwordEncoder);
            User admin = createUser("admin@motivise.app", "motadmin",
                "AdminPass456!", "CH", Role.ADMIN, adminAvatarUrl, passwordEncoder);

            userRepository.saveAll(List.of(anna, max, admin));
            
            // Create test posts
            Post post1 = createPost("webengineering", 
                "Just finished my Java Spring Boot tutorial! 🚀", anna);
            Post post2 = createPost("database", 
                "Playing around with Flyway migrations and MySQL.", max);
            Post post3 = createPost("motivation", 
                "Welcome to Motivise! Keep your 30-day streak alive 💪", admin);
                
            postRepository.saveAll(List.of(post1, post2, post3));
            
            log.info("Test data loaded successfully!");
            printTestCredentials();
            printH2ConnectionInfo();
        };
    }

    /**
     * Loads an avatar from resources, uploads to storage, and returns the URL.
     * Falls back to placeholder URL if resource not found or upload fails.
     */
    private String uploadAvatarFromResources(String resourcePath, MediaService mediaService) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.warn("Avatar resource not found: {}, using placeholder", resourcePath);
                return "https://example.com/images/default.png";
            }

            InputStream inputStream = resource.getInputStream();
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);

            MultipartFile multipartFile = new InMemoryMultipartFile(filename, "image/avif", content);

            Media media = mediaService.upload(multipartFile);
            String avatarUrl = "/medias/" + media.getId();

            log.info("✅ Uploaded avatar: {} -> {}", filename, avatarUrl);
            return avatarUrl;

        } catch (IOException e) {
            log.error("❌ Failed to upload avatar from resources: {}", resourcePath, e);
            return "https://example.com/images/default.png";
        }
    }

    private User createUser(String email, String username, String password,
                           String countryCode, Role role, String profileImageUrl,
                           PasswordEncoder encoder) {
        return new User(email, username, encoder.encode(password),
                       countryCode, profileImageUrl, role);
    }
    
    private Post createPost(String subject, String content, User author) {
        return new Post(subject, content, null, author);
    }
    
    private void printTestCredentials() {
        log.info("Test Credentials:");
        log.info("   anna.schmidt@example.com / Password123!");
        log.info("   max.meier@example.com / Password123!");
        log.info("   admin@motivise.app / AdminPass456!");
    }
    
    private void printH2ConnectionInfo() {
        log.info("H2 Console Access (In-Memory DB):");
        log.info("   URL: http://localhost:8081/h2-console");
        log.info("   JDBC URL: jdbc:h2:mem:motivise_dev");
        log.info("   Username: sa");
        log.info("   Password: (leave empty)");
        log.info("   In-memory only - data lost on restart!");
    }

    /**
     * Simple in-memory implementation of MultipartFile for test data loading.
     */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String filename;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String filename, String contentType, byte[] content) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }
}
