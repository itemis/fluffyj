package de.itemis.fluffyj.concurrency;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import de.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to work with {@link CountDownLatch latches}.
 */
public final class FluffyLatches {

    private FluffyLatches() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Wait on the provided {@code latch} to reach zero. Waits for a maximum of {@code timeout}
     * time. Precision is milliseconds.
     * </p>
     * <p>
     * If waiting is interrupted, the method preserves the interrupt flag and throws a
     * {@link RuntimeException}.
     * </p>
     *
     * @param latch - Wait on this latch.
     * @param timeout - Wait for as long as timeout. Precision is milliseconds.
     * @return {@code true} if count has reached zero. {@code false} otherwise.
     * @throws RuntimeException If the waiting thread is interrupted.
     */
    public static boolean waitOnLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean result = false;
        try {
            result = latch.await(timeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Was interrupted while waiting on latch to become zero.", e);
        }

        return result;
    }
}
