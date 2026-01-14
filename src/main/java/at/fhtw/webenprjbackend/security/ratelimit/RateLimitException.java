package at.fhtw.webenprjbackend.security.ratelimit;

/**
 * Exception thrown when a client exceeds the rate limit for login attempts.
 */
public class RateLimitException extends RuntimeException {

    private final int retryAfterSeconds;

    public RateLimitException(int retryAfterSeconds) {
        super("Rate limit exceeded. Retry after " + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
