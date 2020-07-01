package ru.ppr.utils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.ppr.logger.Logger;

/**
 * Фабричные методы для генерации {@link Executor}
 *
 * @author Aleksandr Brazhkin
 */
public class Executors {

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed.
     *
     * @param tag           the tag for log
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newLoggableSingleThreadExecutor(final String tag, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                logErrorAfterExecute(tag, r, t);
            }
        };
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time.
     *
     * @param tag           the tag for log
     * @param threadFactory the factory to use when creating new threads
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newLoggableSingleThreadScheduledExecutor(final String tag, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(1, threadFactory) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                logErrorAfterExecute(tag, r, t);
            }
        };
    }

    public static void logErrorAfterExecute(String tag, Runnable r, Throwable t) {
        if (t == null
                && r instanceof Future<?>
                && ((Future<?>) r).isDone()) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                // ignore/reset
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            Logger.error(tag, t);
        }
    }
}
