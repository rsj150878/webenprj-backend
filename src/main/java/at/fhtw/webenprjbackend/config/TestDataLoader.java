package at.fhtw.webenprjbackend.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import at.fhtw.webenprjbackend.entity.Follow;
import at.fhtw.webenprjbackend.entity.Media;
import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.PostLike;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.FollowRepository;
import at.fhtw.webenprjbackend.repository.PostLikeRepository;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;
import at.fhtw.webenprjbackend.service.MediaService;

@Configuration
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);
    private static final Random random = new Random(42);

    @Value("${app.user.default-profile-image}")
    private String defaultProfileImage;

    private static final String[] FIRST_NAMES = {
        "Emma", "Liam", "Sofia", "Noah", "Mia", "Lucas", "Anna", "Felix", "Laura", "Max"
    };

    private static final String[] LAST_NAMES = {
        "Mueller", "Schmidt", "Weber", "Fischer", "Meyer", "Wagner", "Becker", "Hoffmann", "Koch", "Wolf"
    };

    private static final String[] COUNTRIES = {"AT", "DE", "CH", "US", "UK"};

    private static final String[] SUBJECTS = {
        "webdev", "database", "algorithms", "java", "python", "javascript", "spring", "docker"
    };

    private static final String[] POST_TEMPLATES = {
        "Just finished my %s tutorial!",
        "Day %s of learning %s.",
        "Finally understood %s after weeks of practice!",
        "My %s project is coming along nicely!",
        "Completed the %s certification exam!"
    };

    private static final String[] COMMENT_TEMPLATES = {
        "Nice work!", "Thanks for sharing!", "Same here!", "Keep it up!", "Helpful post!"
    };

    @Bean
    @ConditionalOnProperty(name = "app.data.load-dev-data", havingValue = "true")
    CommandLineRunner initDatabase(UserRepository userRepository,
                                 PostRepository postRepository,
                                 FollowRepository followRepository,
                                 PostLikeRepository postLikeRepository,
                                 PasswordEncoder passwordEncoder,
                                 MediaService mediaService) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already has data, skipping");
                return;
            }

            log.info("Loading test data...");

            // Upload avatars
            String annaAvatar = uploadAvatar("test-data/avatars/anna-avatar.avif", mediaService);
            String maxAvatar = uploadAvatar("test-data/avatars/max-avatar.avif", mediaService);
            String adminAvatar = uploadAvatar("test-data/avatars/admin-avatar.avif", mediaService);

            // Create users
            List<User> users = new ArrayList<>();

            users.add(createUser("anna.schmidt@example.com", "study_anna", "Password123!", "AT", Role.USER, annaAvatar, passwordEncoder));
            users.add(createUser("max.meier@example.com", "maxlearns", "Password123!", "DE", Role.USER, maxAvatar, passwordEncoder));
            users.add(createUser("admin@motivise.app", "motadmin", "AdminPass456!", "CH", Role.ADMIN, adminAvatar, passwordEncoder));
            users.add(createUser("moderator@motivise.app", "mod_helper", "AdminPass456!", "AT", Role.ADMIN, defaultProfileImage, passwordEncoder));

            for (int i = 0; i < 46; i++) {
                String firstName = FIRST_NAMES[i % FIRST_NAMES.length];
                String lastName = LAST_NAMES[(i * 7) % LAST_NAMES.length];
                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + (i > 0 ? i : "") + "@example.com";
                String username = firstName.toLowerCase() + "_" + lastName.toLowerCase().substring(0, 3) + (i > 0 ? i : "");
                String country = COUNTRIES[i % COUNTRIES.length];
                users.add(createUser(email, username, "Password123!", country, Role.USER, defaultProfileImage, passwordEncoder));
            }
            userRepository.saveAll(users);

            // Create posts
            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                User author = users.get(i % users.size());
                String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
                String template = POST_TEMPLATES[random.nextInt(POST_TEMPLATES.length)];
                String content = String.format(template, subject, random.nextInt(100) + 1, subject);
                posts.add(new Post(subject, content, null, author));
            }
            postRepository.saveAll(posts);

            // Create comments
            List<Post> comments = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Post parent = posts.get(random.nextInt(posts.size()));
                User commenter = users.get(random.nextInt(users.size()));
                Post comment = new Post(parent.getSubject(), COMMENT_TEMPLATES[random.nextInt(COMMENT_TEMPLATES.length)], null, commenter);
                comment.setParent(parent);
                comments.add(comment);
            }
            postRepository.saveAll(comments);

            // Create follows
            List<Follow> follows = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                User follower = users.get(random.nextInt(users.size()));
                User followed = users.get(random.nextInt(users.size()));
                if (!follower.equals(followed) && follows.stream().noneMatch(f -> f.getFollower().equals(follower) && f.getFollowed().equals(followed))) {
                    follows.add(new Follow(follower, followed));
                }
            }
            followRepository.saveAll(follows);

            // Create likes
            List<PostLike> likes = new ArrayList<>();
            for (int i = 0; i < 300; i++) {
                User liker = users.get(random.nextInt(users.size()));
                Post post = posts.get(random.nextInt(posts.size()));
                if (likes.stream().noneMatch(l -> l.getUser().equals(liker) && l.getPost().equals(post))) {
                    likes.add(new PostLike(liker, post));
                }
            }
            postLikeRepository.saveAll(likes);

            log.info("Loaded: {} users, {} posts, {} comments, {} follows, {} likes",
                    users.size(), posts.size(), comments.size(), follows.size(), likes.size());
            log.info("Test logins: anna.schmidt@example.com / max.meier@example.com / admin@motivise.app (Password123! or AdminPass456!)");
        };
    }

    private String uploadAvatar(String path, MediaService mediaService) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) return defaultProfileImage;

            byte[] content = resource.getInputStream().readAllBytes();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            Media media = mediaService.upload(new InMemoryMultipartFile(filename, "image/avif", content));
            return "/medias/" + media.getId();
        } catch (IOException e) {
            log.warn("Failed to upload {}: {}", path, e.getMessage());
            return defaultProfileImage;
        }
    }

    private User createUser(String email, String username, String password, String country, Role role, String avatar, PasswordEncoder encoder) {
        return new User(email, username, encoder.encode(password), country, avatar, role);
    }

    private static class InMemoryMultipartFile implements MultipartFile {
        private final String filename;
        private final String contentType;
        private final byte[] content;

        InMemoryMultipartFile(String filename, String contentType, byte[] content) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content == null || content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) { throw new UnsupportedOperationException(); }
    }
}
