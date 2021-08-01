package com.itemis.fluffyj.concurrency;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to make working with {@link Future} more straight forward.
 */
public final class FluffyFutures {

    private FluffyFutures() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Wait on the provided {@code future} to return a value for as long as {@code timeout}.
     * </p>
     * <p>
     * This method throws a {@link RuntimeException} if:
     * <ul>
     * <li>It is interrupted while waiting.</li>
     * <li>The timeout is reached.</li>
     * <li>The future's execution code did throw an exception.</li>
     * </ul>
     * </p>
     * <p>
     * If waiting is interrupted the thread's interrupt flag will be preserved.
     * </p>
     * <p>
     * If the future threw an exception, this exception will be attached to the
     * {@link RuntimeException} as cause.
     * </p>
     *
     * @param <T> - The return type of the value produced by the provided {@code future}.
     * @param future - Wait on this {@link Future}.
     * @param timeout - Wait for as long as this duration. Precision is milliseconds.
     * @return The value produced by the {@code future} or {@code null} if none was produced.
     * @throws RuntimeException in case something goes wrong.
     */
    public static <T> T waitOnFuture(Future<T> future, Duration timeout) {
        requireNonNull(future, "future");
        requireNonNull(timeout, "timeout");

        T result = null;
        try {
            result = future.get(timeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Was interrupted while waiting on latch to become zero.", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Waiting on Future to return a value timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("While waiting on future to finish: Future threw exception.", cause);
        }

        return result;
    }
}
