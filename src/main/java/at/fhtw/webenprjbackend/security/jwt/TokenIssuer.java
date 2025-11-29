package at.fhtw.webenprjbackend.security.jwt;

import java.util.UUID;

public interface TokenIssuer {

    /**
     * issues a JWT for a user
     *
     * @param userId   ID users
     * @param username Username or Email
     * @param role
     * @return signed JWT as String
     */
    String issue(UUID userId, String username, String role);
}
