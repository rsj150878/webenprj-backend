package at.fhtw.webenprjbackend.security.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks login attempts for a single client IP within a time window.
 * Thread-safe for concurrent access.
 */
public class RateLimitEntry {

    private static final int WINDOW_SECONDS = 60;

    private final long windowStart;
    private final AtomicInteger attempts;

    public RateLimitEntry() {
        this.windowStart = System.currentTimeMillis();
        this.attempts = new AtomicInteger(1);
    }

    /**
     * Checks if the time window has expired.
     */
    public boolean isExpired() {
        long elapsedMs = System.currentTimeMillis() - windowStart;
        return elapsedMs >= WINDOW_SECONDS * 1000L;
    }

    /**
     * Increments the attempt counter.
     */
    public void increment() {
        attempts.incrementAndGet();
    }

    /**
     * Returns the current number of attempts in this window.
     */
    public int getAttempts() {
        return attempts.get();
    }

    /**
     * Returns the number of seconds until the window resets.
     */
    public int getSecondsUntilReset() {
        long elapsedMs = System.currentTimeMillis() - windowStart;
        long remainingMs = (WINDOW_SECONDS * 1000L) - elapsedMs;
        return Math.max(1, (int) Math.ceil(remainingMs / 1000.0));
    }
}
