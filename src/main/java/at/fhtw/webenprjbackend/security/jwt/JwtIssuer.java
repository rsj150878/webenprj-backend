package at.fhtw.webenprjbackend.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtIssuer implements TokenIssuer {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    public JwtIssuer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void initKey() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(UUID userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(username)                  // subject = login identifier (e.g. email)
                .claim("uid", userId.toString())    // user id
                .claim("role", role)                // e.g. ROLE_USER, ROLE_ADMIN
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

}
