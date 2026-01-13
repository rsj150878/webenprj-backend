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

/**
 * Configurable test data loader.
 * Controlled by app.data.load-dev-data property.
 */
@Configuration
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);
    private static final Random random = new Random(42); // Fixed seed for reproducibility

    @Value("${app.user.default-profile-image}")
    private String defaultProfileImage;

    // Data arrays for generation
    private static final String[] FIRST_NAMES = {
        "Emma", "Liam", "Sofia", "Noah", "Mia", "Lucas", "Olivia", "Elias",
        "Hannah", "Felix", "Anna", "Paul", "Laura", "Leon", "Marie", "Ben",
        "Lea", "Jonas", "Lena", "Tim", "Sarah", "David", "Julia", "Lukas",
        "Lisa", "Finn", "Emily", "Max", "Sophia", "Jan", "Amelie", "Tom",
        "Klara", "Nico", "Emilia", "Moritz", "Johanna", "Erik", "Nele", "Simon",
        "Ida", "Philipp", "Helena", "Fabian", "Charlotte", "Sebastian", "Victoria", "Alexander"
    };

    private static final String[] LAST_NAMES = {
        "Mueller", "Schmidt", "Weber", "Fischer", "Meyer", "Wagner", "Becker", "Schulz",
        "Hoffmann", "Koch", "Richter", "Klein", "Wolf", "Schroeder", "Neumann", "Schwarz",
        "Braun", "Zimmermann", "Krueger", "Hofmann", "Hartmann", "Lange", "Werner", "Krause",
        "Lehmann", "Koehler", "Maier", "Hermann", "Koenig", "Huber", "Kaiser", "Fuchs",
        "Peters", "Lang", "Scholz", "Moeller", "Weiss", "Jung", "Hahn", "Vogel"
    };

    private static final String[] COUNTRIES = {
        "AT", "DE", "CH", "US", "UK", "FR", "IT", "ES", "NL", "BE", "PL", "SE", "NO", "DK", "FI"
    };

    private static final String[] SUBJECTS = {
        "webdev", "database", "motivation", "algorithms", "design", "java", "python",
        "javascript", "react", "vue", "spring", "docker", "kubernetes", "devops",
        "machinelearning", "datascience", "security", "networking", "cloud", "mobile"
    };

    private static final String[] POST_TEMPLATES = {
        "Just finished my %s tutorial! Feeling accomplished.",
        "Day %s of learning %s. Progress is slow but steady.",
        "Anyone else struggling with %s concepts?",
        "Finally understood %s after weeks of practice!",
        "Great resources for %s: check out the official docs!",
        "Study session complete. Covered %s today.",
        "Pro tip for %s: always read the documentation first.",
        "My %s project is coming along nicely!",
        "Debugging %s code at 2am... who else?",
        "Excited to share my %s learning journey!",
        "Question about %s: anyone know good tutorials?",
        "Breakthrough moment with %s today!",
        "Taking a break from %s to clear my head.",
        "Just deployed my first %s application!",
        "Study group for %s? DM me if interested!",
        "The %s community is so helpful!",
        "Completed the %s certification exam!",
        "Working on a %s side project this weekend.",
        "Best practices for %s I learned today...",
        "Motivation tip: start small with %s and build up!"
    };

    private static final String[] COMMENT_TEMPLATES = {
        "Great post! I'm learning this too.",
        "Thanks for sharing!",
        "This is really helpful.",
        "I had the same experience!",
        "Keep up the good work!",
        "Can you share more details?",
        "Bookmarked for later!",
        "Inspiring stuff!",
        "I needed to see this today.",
        "Awesome progress!"
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
                log.info("Database already contains data, skipping test data load");
                return;
            }

            log.info("Loading massive test data for development...");

            // Upload avatars for featured users (fallback to default if upload fails)
            log.info("Uploading test user avatars...");
            String annaAvatarUrl = uploadAvatarOrDefault("test-data/avatars/anna-avatar.avif", mediaService);
            String maxAvatarUrl = uploadAvatarOrDefault("test-data/avatars/max-avatar.avif", mediaService);
            String adminAvatarUrl = uploadAvatarOrDefault("test-data/avatars/admin-avatar.avif", mediaService);

            // Create users
            log.info("Creating 50 users...");
            List<User> users = new ArrayList<>();

            // Featured users with avatars
            User anna = createUser("anna.schmidt@example.com", "study_anna", "Password123!", "AT", Role.USER, annaAvatarUrl, passwordEncoder);
            User max = createUser("max.meier@example.com", "maxlearns", "Password123!", "DE", Role.USER, maxAvatarUrl, passwordEncoder);
            User admin = createUser("admin@motivise.app", "motadmin", "AdminPass456!", "CH", Role.ADMIN, adminAvatarUrl, passwordEncoder);
            User admin2 = createUser("moderator@motivise.app", "mod_helper", "AdminPass456!", "AT", Role.ADMIN, defaultProfileImage, passwordEncoder);

            users.add(anna);
            users.add(max);
            users.add(admin);
            users.add(admin2);

            // Generate 46 more users
            for (int i = 0; i < 46; i++) {
                String firstName = FIRST_NAMES[i % FIRST_NAMES.length];
                String lastName = LAST_NAMES[(i * 7) % LAST_NAMES.length];
                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + (i > 0 ? i : "") + "@example.com";
                String username = firstName.toLowerCase() + "_" + lastName.toLowerCase().substring(0, Math.min(3, lastName.length()));
                if (i > 0) username += i;
                String country = COUNTRIES[i % COUNTRIES.length];

                users.add(createUser(email, username, "Password123!", country, Role.USER, defaultProfileImage, passwordEncoder));
            }

            userRepository.saveAll(users);
            log.info("Created {} users", users.size());

            // Create posts
            log.info("Creating 500 posts...");
            List<Post> posts = new ArrayList<>();

            for (int i = 0; i < 500; i++) {
                User author = users.get(i % users.size());
                String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
                String template = POST_TEMPLATES[random.nextInt(POST_TEMPLATES.length)];
                String content = String.format(template, subject, String.valueOf(random.nextInt(100) + 1), subject);

                posts.add(createPost(subject, content, author));
            }

            postRepository.saveAll(posts);
            log.info("Created {} posts", posts.size());

            // Create comments (replies)
            log.info("Creating ~100 comments...");
            List<Post> comments = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                Post parentPost = posts.get(random.nextInt(posts.size()));
                User commenter = users.get(random.nextInt(users.size()));
                String commentContent = COMMENT_TEMPLATES[random.nextInt(COMMENT_TEMPLATES.length)];

                Post comment = new Post(parentPost.getSubject(), commentContent, null, commenter);
                comment.setParent(parentPost);
                comments.add(comment);
            }

            postRepository.saveAll(comments);
            log.info("Created {} comments", comments.size());

            // Create follow relationships
            log.info("Creating ~200 follow relationships...");
            List<Follow> follows = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                User follower = users.get(random.nextInt(users.size()));
                User followed = users.get(random.nextInt(users.size()));

                if (!follower.equals(followed) && !followExists(follows, follower, followed)) {
                    follows.add(new Follow(follower, followed));
                }
            }

            followRepository.saveAll(follows);
            log.info("Created {} follow relationships", follows.size());

            // Create likes
            log.info("Creating ~300 likes...");
            List<PostLike> likes = new ArrayList<>();

            for (int i = 0; i < 300; i++) {
                User liker = users.get(random.nextInt(users.size()));
                Post likedPost = posts.get(random.nextInt(posts.size()));

                if (!likeExists(likes, liker, likedPost)) {
                    likes.add(new PostLike(liker, likedPost));
                }
            }

            postLikeRepository.saveAll(likes);
            log.info("Created {} likes", likes.size());

            log.info("Test data loaded successfully!");
            log.info("Summary: {} users, {} posts, {} comments, {} follows, {} likes",
                    users.size(), posts.size(), comments.size(), follows.size(), likes.size());
            printTestCredentials();
            printH2ConnectionInfo();
        };
    }

    private boolean followExists(List<Follow> follows, User follower, User followed) {
        return follows.stream().anyMatch(f ->
            f.getFollower().equals(follower) && f.getFollowed().equals(followed));
    }

    private boolean likeExists(List<PostLike> likes, User user, Post post) {
        return likes.stream().anyMatch(l ->
            l.getUser().equals(user) && l.getPost().equals(post));
    }

    private String uploadAvatarOrDefault(String resourcePath, MediaService mediaService) {
        String url = uploadAvatarFromResources(resourcePath, mediaService);
        return url != null ? url : defaultProfileImage;
    }

    private String uploadAvatarFromResources(String resourcePath, MediaService mediaService) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.warn("Avatar resource not found: {}, using placeholder", resourcePath);
                return null;
            }

            InputStream inputStream = resource.getInputStream();
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            MultipartFile multipartFile = new InMemoryMultipartFile(filename, "image/avif", content);
            Media media = mediaService.upload(multipartFile);
            String avatarUrl = "/medias/" + media.getId();

            log.info("Uploaded avatar: {} -> {}", filename, avatarUrl);
            return avatarUrl;

        } catch (IOException e) {
            log.error("Failed to upload avatar from resources: {}", resourcePath, e);
            return null;
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
        log.info("=== Test Credentials ===");
        log.info("  anna.schmidt@example.com / Password123!");
        log.info("  max.meier@example.com / Password123!");
        log.info("  admin@motivise.app / AdminPass456!");
        log.info("  moderator@motivise.app / AdminPass456!");
        log.info("  (All other users: Password123!)");
    }

    private void printH2ConnectionInfo() {
        log.info("=== H2 Console Access ===");
        log.info("  URL: http://localhost:8081/h2-console");
        log.info("  JDBC URL: jdbc:h2:mem:motivise_dev");
        log.info("  Username: sa | Password: (empty)");
    }

    private static class InMemoryMultipartFile implements MultipartFile {
        private final String filename;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String filename, String contentType, byte[] content) {
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
