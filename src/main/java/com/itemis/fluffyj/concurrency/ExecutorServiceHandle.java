package com.itemis.fluffyj.concurrency;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.ThreadSafe;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * Convenience wrapper for an {@link ExecutorService}. The implementation of this class is
 * threadsafe.
 * </p>
 * <p>
 * The kind of {@link ExecutorService} used by this implementation is at the discretion of this
 * class.
 * </p>
 */
@ThreadSafe
public final class ExecutorServiceHandle {

    private final int threadCount;
    private final ThreadFactory threadFactory;

    private volatile ExecutorService executorService;

    /**
     * Construct a new instance.
     *
     * @param threadCount - How many threads may be running in parallel at max? Must be greater than
     *        or equal to zero (gte 0).
     * @param threadNameFactory - All threads will have names created by this factory.
     */
    public ExecutorServiceHandle(final int threadCount, final ThreadNameFactory threadNameFactory) {
        checkArgument(threadCount > 0, "Thread count must be gte 0.");
        requireNonNull(threadNameFactory, "threadNameFactory");

        this.threadCount = threadCount;
        this.threadFactory = task -> {
            final var id = threadNameFactory.generate();
            return new Thread(task, id);
        };
    }

    /**
     * <p>
     * Accessor for the {@link ExecutorService}. The first invocation of this method will create the
     * {@link ExecutorService}. Subsequent invocations of this method will return the created
     * instance.
     * </p>
     *
     * @return The {@link ExecutorService} wrapped by this instance.
     */
    public ExecutorService getExecutor() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(threadCount, threadFactory);
                }
            }
        }
        return executorService;
    }

    /**
     * <p>
     * Force the {@link ExecutorService} to terminate. Sends an interrupt to all running tasks and
     * does not schedule / accept new tasks. It is designed in such a way so that it may not throw
     * any exceptions. This has been done on purpose on order to make sure, subsequent code may get
     * executed. This is thought to reduce resource leaks from not executed cleanup code.
     * </p>
     * <p>
     * This method blocks for {@code timeout} time to give running tasks a chance to shut down. If
     * the time runs out before every task has shut down, it will return {@code false}.
     * </p>
     * <p>
     * While blocking, the method will respect thread interrupts. In this case, it will set the
     * thread's interrupt flag and return {@code false}.
     * </p>
     * <p>
     * When this method returns, the {@link ExecutorService} is rendered useless and this instance
     * should be discarded.
     * </p>
     *
     * @return {@code true} if all running tasks could be stopped. {@code false} otherwise.
     */
    public boolean kill(final Duration timeout) {
        requireNonNull(timeout, "timeout");
        return FluffyExecutors.kill(getExecutor(), timeout);
    }
}
