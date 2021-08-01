package com.itemis.fluffyj.tests;

import static com.itemis.fluffyj.concurrency.FluffyLatches.waitOnLatch;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to work with {@link CountDownLatch latches}.
 */
public final class InternalTestLatches {

    private InternalTestLatches() {
        throw new InstantiationNotPermittedException();
    }

    /**
     *
     * @param latch
     * @param timeout
     */
    public static void assertLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean latchWasZero = waitOnLatch(latch, timeout);
        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();
    }
}
