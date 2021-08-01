package com.itemis.fluffyj.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.tests.InternalTestFutures.scheduleInterruptibleFuture;
import static com.itemis.fluffyj.tests.InternalTestFutures.scheduleNeverendingFuture;
import static com.itemis.fluffyj.tests.InternalTestLatches.assertLatch;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExecutorServiceHandleTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(500);
    private static final int THREAD_COUNT = 100;
    private static final String EXPECTED_THREAD_NAME = "expectedThreadName";

    private ThreadNameFactory threadNameFactoryMock;
    private List<ExecutorService> createdExecutors = new CopyOnWriteArrayList<>();
    private ExecutorService executor;

    private ExecutorServiceHandle underTest;

    @BeforeEach
    public void setUp() {
        threadNameFactoryMock = () -> EXPECTED_THREAD_NAME;
        executor = newFixedThreadPool(THREAD_COUNT);
        underTest = constructUnderTest(threadNameFactoryMock);
    }

    @AfterEach
    public void tearDown() {
        kill(executor, DEFAULT_TIMEOUT);
    }

    @Test
    public void get_executor_returns_executor() {
        assertThat(underTest.getExecutor()).isNotNull();
    }

    @Test
    public void calling_get_executor_twice_returns_same_executor_instance() {
        ExecutorService firstResult = underTest.getExecutor();
        ExecutorService secondResult = underTest.getExecutor();
        assertThat(firstResult).isSameAs(secondResult);
    }

    @Test
    public void get_executor_is_thread_safe() {
        var executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> createdExecutors.add(underTest.getExecutor()));
        }

        executor.shutdown();
        var firstExec = createdExecutors.get(0);
        assertThat(createdExecutors).as("getter must always return the same instance.").allMatch(exec -> exec == firstExec);
    }

    @Test
    public void executor_is_shutdown_after_kill() {
        var localExecutor = underTest.getExecutor();
        underTest.kill(DEFAULT_TIMEOUT);
        assertThat(localExecutor.isShutdown()).as("Executor was not shutdown.").isTrue();
    }

    @Test
    public void kill_cancels_all_running_tasks_and_returns_true() {
        List<Future<?>> futures = new ArrayList<>();
        var localExecutor = underTest.getExecutor();
        futures.add(scheduleInterruptibleFuture(localExecutor));

        assertThat(kill(localExecutor, DEFAULT_TIMEOUT)).as("Method must return true if all tasks have been canceled in time.").isTrue();
        assertThat(futures).as("Kill on executor did not stop running future.").allSatisfy(future -> future.isDone());
    }

    @Test
    public void kill_returns_false_if_not_all_tasks_could_be_canceled() {
        var localExecutor = underTest.getExecutor();
        var neverendingFuture = scheduleNeverendingFuture(localExecutor);
        try {
            assertThat(kill(localExecutor, DEFAULT_TIMEOUT)).as("kill must return false if executor could not end all tasks.").isFalse();
        } finally {
            neverendingFuture.stop();
        }
    }

    @Test
    public void if_kill_is_interrupted_it_returns_false_and_preserves_interrupted_flag() {
        var localExecutor = underTest.getExecutor();
        var neverendingFuture = scheduleNeverendingFuture(localExecutor);
        var latch = new CountDownLatch(1);
        AtomicBoolean killReturnValue = new AtomicBoolean(false);
        AtomicBoolean interruptFlagSet = new AtomicBoolean(false);

        executor.submit(() -> {
            latch.countDown();
            killReturnValue.set(kill(localExecutor, DEFAULT_TIMEOUT));
            interruptFlagSet.set(currentThread().isInterrupted());
        });

        try {
            assertLatch(latch, DEFAULT_TIMEOUT);
            kill(executor, DEFAULT_TIMEOUT);

            assertThat(killReturnValue).as("kill must return false if interrupted.").isFalse();
            assertThat(interruptFlagSet).as("kill must preserve interrupted flag if interrupted.").isTrue();
        } finally {
            neverendingFuture.stop();
        }
    }

    @Test
    public void constructor_takes_thread_count_into_account() {
        var localExecutor = underTest.getExecutor();
        AtomicInteger actualThreadCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        for (int i = 0; i < THREAD_COUNT; i++) {
            localExecutor.submit(() -> {
                startLatch.countDown();
                actualThreadCount.getAndIncrement();
                assertLatch(releaseLatch, DEFAULT_TIMEOUT);
            });
        }

        assertLatch(startLatch, DEFAULT_TIMEOUT);
        releaseLatch.countDown();
        assertThat(actualThreadCount.get()).as("Encountered unexpected thread count.").isEqualTo(THREAD_COUNT);
    }

    @Test
    public void created_threads_have_specified_name() {
        var latch = new CountDownLatch(1);

        AtomicReference<String> actualThreadName = new AtomicReference<>();
        underTest.getExecutor().submit(() -> {
            actualThreadName.set(currentThread().getName());
            latch.countDown();
        });

        assertLatch(latch, DEFAULT_TIMEOUT);
        assertThat(actualThreadName.get()).as("Encountered unexpected thread name.").isEqualTo(EXPECTED_THREAD_NAME);
    }

    @Test
    public void constructor_does_not_accept_invalid_thread_count() {
        String expectedMessage = "Thread count must be gte 0.";

        assertThatThrownBy(() -> new ExecutorServiceHandle(-1, threadNameFactoryMock)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);

        assertThatThrownBy(() -> new ExecutorServiceHandle(0, threadNameFactoryMock)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    private ExecutorServiceHandle constructUnderTest(ThreadNameFactory threadNameFactory) {
        return new ExecutorServiceHandle(THREAD_COUNT, threadNameFactoryMock);
    }
}
