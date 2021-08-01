package com.itemis.fluffyj.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.tests.InternalTestFutures.scheduleInterruptibleFuture;
import static com.itemis.fluffyj.tests.InternalTestFutures.scheduleNeverendingFuture;
import static com.itemis.fluffyj.tests.InternalTestLatches.assertLatch;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FluffyExecutorsTest {

    private static final int DEFAULT_TIMEOUT_MILLIS = 500;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(DEFAULT_TIMEOUT_MILLIS);
    private static final int THREAD_COUNT = 2;

    private ExecutorService executor;
    private ExecutorService anotherExecutor;

    @BeforeEach
    public void setUp() {
        executor = newFixedThreadPool(THREAD_COUNT);
        anotherExecutor = newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
        anotherExecutor.shutdownNow();
        boolean done = false;

        try {
            done = executor.awaitTermination(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                && anotherExecutor.awaitTermination(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            done = false;
        }

        if (!done) {
            System.err.println("[WARNING] Possible resource leak: Executor could possibly not terminate all tasks.");
        }
    }

    @Test
    public void executor_is_shutdown_after_kill() {
        kill(executor, DEFAULT_TIMEOUT);
        assertThat(executor.isShutdown()).as("Executor was not shutdown.").isTrue();
    }

    @Test
    public void kill_cancels_all_running_tasks_and_returns_true() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(scheduleInterruptibleFuture(executor));
        }

        assertThat(kill(executor, DEFAULT_TIMEOUT)).as("Method must return true if all tasks have been canceled in time.").isTrue();
        assertThat(futures).as("Kill on executor did not stop running future.").allSatisfy(future -> future.isDone());
    }

    @Test
    public void kill_returns_false_if_not_all_tasks_could_be_canceled() {
        var neverendingFuture = scheduleNeverendingFuture(executor);
        try {
            assertThat(kill(executor, DEFAULT_TIMEOUT)).as("kill must return false if executor could not end all tasks.").isFalse();
        } finally {
            neverendingFuture.stop();
        }
    }

    @Test
    public void if_kill_is_interrupted_it_returns_false_and_preserves_interrupted_flag() {
        var neverendingFuture = scheduleNeverendingFuture(executor);
        var latch = new CountDownLatch(1);
        AtomicBoolean killReturnValue = new AtomicBoolean(false);
        AtomicBoolean interruptFlagSet = new AtomicBoolean(false);

        anotherExecutor.submit(() -> {
            latch.countDown();
            killReturnValue.set(kill(executor, DEFAULT_TIMEOUT));
            interruptFlagSet.set(Thread.currentThread().isInterrupted());
        });

        try {
            assertLatch(latch, DEFAULT_TIMEOUT);
            kill(anotherExecutor, DEFAULT_TIMEOUT);

            assertThat(killReturnValue).as("kill must return false if interrupted.").isFalse();
            assertThat(interruptFlagSet).as("kill must preserve interrupted flag if interrupted.").isTrue();
        } finally {
            neverendingFuture.stop();
        }
    }
}
