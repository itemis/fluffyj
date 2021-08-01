package com.itemis.fluffyj.concurrency;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods for working with {@link ExecutorService executors}.
 */
public final class FluffyExecutors {

    private FluffyExecutors() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Force the provided {@link ExecutorService} to terminate. Sends an interrupt to all running
     * tasks and does not schedule / accept new tasks. It is designed in such a way so that it may
     * not throw any exceptions. This has been done on purpose on order to make sure, subsequent
     * code may get executed. This is thought to reduce resource leaks from not executed cleanup
     * code.
     * </p>
     * <p>
     * This method blocks for {@code timeout} time to give running tasks a chance to shut down. If
     * the time runs out before every task has shut down, it will return {@code false}.
     * </p>
     * <p>
     * While blocking, the method will respect thread interrupts. In this case, it will set the
     * thread's interrupt flag and return {@code false}.
     * </p>
     *
     * @param executor
     * @return {@code true} if all running tasks could be stopped. {@code false} otherwise.
     */
    public static boolean kill(ExecutorService executor, Duration timeout) {
        requireNonNull(executor, "executor");
        requireNonNull(timeout, "timeout");

        executor.shutdownNow();
        boolean result = false;
        try {
            result = executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }
}
