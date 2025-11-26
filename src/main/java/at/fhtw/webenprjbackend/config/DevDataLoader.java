package at.fhtw.webenprjbackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * DEVELOPMENT ONLY: Loads test data with hardcoded credentials.
 * WARNING: Never use these credentials in production environments.
 * This class only runs when the 'dev' profile is active.
 */

@Configuration
@Profile("dev")
public class DevDataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                 PostRepository postRepository,
                                 PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("[DEV] Loading development test data...");
            
            // Create test users with properly hashed passwords
            // Let JPA generate IDs and timestamps automatically
            User anna = new User(
                "anna.schmidt@example.com",
                "study_anna", 
                passwordEncoder.encode("Password123!"),
                "AT",
                "https://example.com/images/profile1.png",
                Role.USER
            );
            userRepository.save(anna);

            User max = new User(
                "max.meier@example.com",
                "maxlearns",
                passwordEncoder.encode("Password123!"), 
                "DE",
                "https://example.com/images/profile2.png",
                Role.USER
            );
            userRepository.save(max);

            User admin = new User(
                "admin@motivise.app",
                "motadmin",
                passwordEncoder.encode("AdminPass456!"),
                "CH", 
                "https://example.com/images/admin.png",
                Role.ADMIN
            );
            userRepository.save(admin);

            // Create test posts using the constructor
            // Let JPA generate IDs and timestamps automatically
            Post post1 = new Post(
                "webengineering",
                "Just finished my Java Spring Boot tutorial! 🚀 Building REST APIs is actually quite fun when you understand the concepts.",
                null,
                anna
            );
            postRepository.save(post1);

            Post post2 = new Post(
                "database", 
                "Playing around with Flyway migrations and MySQL. Database versioning feels powerful.",
                null,
                max
            );
            postRepository.save(post2);

            Post post3 = new Post(
                "motivation",
                "Welcome to Motivise! Share your study progress and keep your 30-day streak alive 💪",
                null,
                admin
            );
            postRepository.save(post3);

            System.out.println("Development test data loaded successfully!");
            System.out.println("Test Users:");
            System.out.println("   anna.schmidt@example.com / Password: Password123!");
            System.out.println("   max.meier@example.com / Password: Password123!");
            System.out.println("   admin@motivise.app / Password: AdminPass456!");
            System.out.println("Sample posts created for all users");
            System.out.println("H2 Console: http://localhost:8081/h2-console");
        };
    }
}
