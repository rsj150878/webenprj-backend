package at.fhtw.webenprjbackend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import at.fhtw.webenprjbackend.entity.Post;
import at.fhtw.webenprjbackend.entity.Role;
import at.fhtw.webenprjbackend.entity.User;
import at.fhtw.webenprjbackend.repository.PostRepository;
import at.fhtw.webenprjbackend.repository.UserRepository;

/**
 * Configurable test data loader.
 * Controlled by app.data.load-dev-data property.
 */
@Configuration
public class TestDataLoader {

    @Bean
    @ConditionalOnProperty(name = "app.data.load-dev-data", havingValue = "true")
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                 PostRepository postRepository,
                                 PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists (prevent duplicates)
            if (userRepository.count() > 0) {
                System.out.println("📊 Database already contains data, skipping test data load");
                return;
            }
            
            System.out.println("🚀 Loading fresh test data for development...");
            
            // Create test users
            User anna = createUser("anna.schmidt@example.com", "study_anna", 
                "Password123!", "AT", Role.USER, passwordEncoder);
            User max = createUser("max.meier@example.com", "maxlearns", 
                "Password123!", "DE", Role.USER, passwordEncoder);
            User admin = createUser("admin@motivise.app", "motadmin", 
                "AdminPass456!", "CH", Role.ADMIN, passwordEncoder);
                
            userRepository.saveAll(List.of(anna, max, admin));
            
            // Create test posts
            Post post1 = createPost("webengineering", 
                "Just finished my Java Spring Boot tutorial! 🚀", anna);
            Post post2 = createPost("database", 
                "Playing around with Flyway migrations and MySQL.", max);
            Post post3 = createPost("motivation", 
                "Welcome to Motivise! Keep your 30-day streak alive 💪", admin);
                
            postRepository.saveAll(List.of(post1, post2, post3));
            
            System.out.println("✅ Test data loaded successfully!");
            printTestCredentials();
            printH2ConnectionInfo();
        };
    }
    
    private User createUser(String email, String username, String password, 
                           String countryCode, Role role, PasswordEncoder encoder) {
        return new User(email, username, encoder.encode(password), 
                       countryCode, "https://example.com/images/default.png", role);
    }
    
    private Post createPost(String subject, String content, User author) {
        return new Post(subject, content, null, author);
    }
    
    private void printTestCredentials() {
        System.out.println("🔑 Test Credentials:");
        System.out.println("   📧 anna.schmidt@example.com / Password123!");
        System.out.println("   📧 max.meier@example.com / Password123!");
        System.out.println("   👑 admin@motivise.app / AdminPass456!");
    }
    
    private void printH2ConnectionInfo() {
        System.out.println("🗄️ H2 Console Access (In-Memory DB):");
        System.out.println("   URL: http://localhost:8081/h2-console");
        System.out.println("   JDBC URL: jdbc:h2:mem:motivise_dev");
        System.out.println("   Username: sa");
        System.out.println("   Password: (leave empty)");
        System.out.println("   ⚡ In-memory only - data lost on restart!");
    }
}
