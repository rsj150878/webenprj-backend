package at.fhtw.webenprjbackend;

import at.fhtw.webenprjbackend.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableConfigurationProperties(JwtProperties.class)
public class WebenprjbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebenprjbackendApplication.class, args);
	}

}
