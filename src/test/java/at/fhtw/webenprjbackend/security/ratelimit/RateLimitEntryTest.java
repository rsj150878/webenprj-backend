package at.fhtw.webenprjbackend.security.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RateLimitEntry}.
 */
@DisplayName("RateLimitEntry")
class RateLimitEntryTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with 1 attempt")
        void constructor_initializesWithOneAttempt() {
            // Act
            RateLimitEntry entry = new RateLimitEntry();

            // Assert
            assertThat(entry.getAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("should not be expired immediately after creation")
        void constructor_notExpiredImmediately() {
            // Act
            RateLimitEntry entry = new RateLimitEntry();

            // Assert
            assertThat(entry.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("increment()")
    class IncrementTests {

        @Test
        @DisplayName("should increment attempt count")
        void increment_increasesAttemptCount() {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();

            // Act
            entry.increment();

            // Assert
            assertThat(entry.getAttempts()).isEqualTo(2);
        }

        @Test
        @DisplayName("should allow multiple increments")
        void increment_multipleIncrements() {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();

            // Act
            entry.increment();
            entry.increment();
            entry.increment();

            // Assert
            assertThat(entry.getAttempts()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("getSecondsUntilReset()")
    class GetSecondsUntilResetTests {

        @Test
        @DisplayName("should return positive value immediately after creation")
        void getSecondsUntilReset_positiveImmediately() {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();

            // Act
            int seconds = entry.getSecondsUntilReset();

            // Assert
            assertThat(seconds).isGreaterThan(0);
            assertThat(seconds).isLessThanOrEqualTo(60);
        }

        @Test
        @DisplayName("should return at least 1 second")
        void getSecondsUntilReset_atLeastOneSecond() {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();

            // Act
            int seconds = entry.getSecondsUntilReset();

            // Assert
            assertThat(seconds).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("isExpired()")
    class IsExpiredTests {

        @Test
        @DisplayName("should return false for fresh entry")
        void isExpired_freshEntry_returnsFalse() {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();

            // Act & Assert
            assertThat(entry.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent increments correctly")
        void concurrentIncrements_correctCount() throws InterruptedException {
            // Arrange
            RateLimitEntry entry = new RateLimitEntry();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(entry::increment);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Assert - initial 1 + 10 increments = 11
            assertThat(entry.getAttempts()).isEqualTo(threadCount + 1);
        }
    }
}
