package com.itemis.fluffyj.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.concurrency.FluffyLatches.waitOnLatch;
import static com.itemis.fluffyj.tests.InternalTestLatches.assertLatch;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FluffyLatchesTest {

    private static final int THREAD_COUNT = 1;
    private static final Duration DEFAULT_TIMEOUT_SHORT = Duration.ofMillis(500);
    private static final Duration DEFAULT_TIMEOUT_LONG = Duration.ofSeconds(2);

    private ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        kill(executor, DEFAULT_TIMEOUT_SHORT);
    }

    @Test
    public void assert_latch_passes_if_latch_is_zero() {
        CountDownLatch latch = new CountDownLatch(0);
        assertDoesNotThrow(() -> assertLatch(latch, DEFAULT_TIMEOUT_SHORT));
    }

    @Test
    public void assert_latch_fails_if_latch_is_non_zero() {
        CountDownLatch latch = new CountDownLatch(1);
        assertThatThrownBy(() -> assertLatch(latch, DEFAULT_TIMEOUT_SHORT)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Waiting on latch to become zero timed out.");
    }

    @Test
    public void assert_latch_waits_timeout() {
        CountDownLatch latch = new CountDownLatch(1);
        Duration expectedDuration = Duration.ofSeconds(1);
        long startTime = System.currentTimeMillis();
        assertThatThrownBy(() -> assertLatch(latch, expectedDuration)).isInstanceOf(AssertionError.class);
        long endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).as("Encountered unexpected assert waiting time.").isGreaterThanOrEqualTo(expectedDuration.toMillis());
    }

    @Test
    public void assert_fails_when_wait_is_interrupted_and_preserves_interrupt_flag() {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch futureStartedLatch = new CountDownLatch(1);

        AtomicBoolean interruptedFlagPreserved = new AtomicBoolean(false);
        Future<?> future = executor.submit(() -> {
            futureStartedLatch.countDown();
            try {
                assertLatch(latch, DEFAULT_TIMEOUT_LONG);
            } catch (Throwable t) {
                interruptedFlagPreserved.set(Thread.currentThread().isInterrupted());
                throw t;
            }
        });

        try {
            futureStartedLatch.await(DEFAULT_TIMEOUT_SHORT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        kill(executor, DEFAULT_TIMEOUT_SHORT);
        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT_SHORT.toMillis(), MILLISECONDS)).isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).getCause().hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }

    @Test
    public void waitOnLatch_passes_if_latch_is_zero() {
        CountDownLatch latch = new CountDownLatch(0);
        assertThat(waitOnLatch(latch, DEFAULT_TIMEOUT_SHORT)).as("Encountered unexpected return value.").isTrue();
    }

    @Test
    public void waitOnLatch_fails_if_latch_is_non_zero() {
        CountDownLatch latch = new CountDownLatch(1);
        assertThat(waitOnLatch(latch, DEFAULT_TIMEOUT_SHORT)).as("Encountered unexpected return value.").isFalse();
    }

    @Test
    public void waitOnLatch_waits_timeout() {
        CountDownLatch latch = new CountDownLatch(1);
        Duration expectedDuration = Duration.ofSeconds(1);
        long startTime = System.currentTimeMillis();
        waitOnLatch(latch, expectedDuration);
        long endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).as("Encountered unexpected waiting time.").isGreaterThanOrEqualTo(expectedDuration.toMillis());
    }

    @Test
    public void waitOnLatch_fails_when_wait_is_interrupted_but_preserves_interrupt_flag() {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch futureStartedLatch = new CountDownLatch(1);

        AtomicBoolean interruptedFlagPreserved = new AtomicBoolean(false);
        Future<?> future = executor.submit(() -> {
            futureStartedLatch.countDown();
            try {
                waitOnLatch(latch, DEFAULT_TIMEOUT_LONG);
            } catch (Throwable t) {
                interruptedFlagPreserved.set(Thread.currentThread().isInterrupted());
                throw t;
            }
        });

        try {
            futureStartedLatch.await(DEFAULT_TIMEOUT_SHORT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        kill(executor, DEFAULT_TIMEOUT_SHORT);
        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT_SHORT.toMillis(), MILLISECONDS)).isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).getCause().hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }
}
