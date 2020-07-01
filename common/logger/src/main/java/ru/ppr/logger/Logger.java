package ru.ppr.logger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dmitry Nevolin on 03.02.2016.
 */
public class Logger {

    private static final String TAG = makeLogTag(Logger.class);

    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(Logger.class);

    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<>();

    private static class Queue {

        private static final ConcurrentLinkedQueue<Runnable> LOG_QUEUE = new ConcurrentLinkedQueue<>();

        private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "Logger"));

        private static void flushOnThreadPool() {
            for (Runnable task : LOG_QUEUE)
                EXECUTOR.execute(task);

            LOG_QUEUE.clear();
        }

        private static void flushOnCurrentThread() {
            for (Runnable task : LOG_QUEUE)
                task.run();

            LOG_QUEUE.clear();
        }

        private static void addInfo(@NonNull String message, org.slf4j.Logger logger) {
            LOG_QUEUE.add(() -> logger.info(message));
        }

        private static void addTrace(@NonNull String message, org.slf4j.Logger logger) {
            LOG_QUEUE.add(() -> logger.trace(message));
        }

        private static void addError(@NonNull String message, org.slf4j.Logger logger) {
            LOG_QUEUE.add(() -> logger.error(message));
        }

        private static void addWarn(@NonNull String message, org.slf4j.Logger logger) {
            LOG_QUEUE.add(() -> logger.warn(message));
        }
    }

    public static void flushQueueSync() {
        Queue.flushOnCurrentThread();
    }

    public static void flushQueueAsync() {
        Queue.flushOnThreadPool();
    }

    private static SimpleDateFormat getDateTimeFormatForLog() {
        SimpleDateFormat format = DATE_TIME_FORMAT.get();
        if (format == null) {
            format = new SimpleDateFormat("HH:mm:ss.SSS");
            DATE_TIME_FORMAT.set(format);
        }
        return format;
    }

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH) {
            return str.substring(0, MAX_LOG_TAG_LENGTH - 1);
        }

        return str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    private static String buildMessage(@NonNull Class<?> c, @NonNull String body) {
        return buildMessage(c.getSimpleName(), body);
    }

    private static String buildMessage(@NonNull String tag, @NonNull String body) {
        return tag + "|" + body;
    }

    private static String addCallTimeAndCurrentThread(@NonNull String message) {
        return "(" + getDateTimeFormatForLog().format(new Date()) + ") [" + Thread.currentThread().getName() + "] " + message;
    }

    ////////////////////// logMethods - start //////////////////////

    public static void debug(@NonNull String message) {
        Log.d(TAG, message);
    }

    public static void debug(@NonNull String tag, @NonNull String message) {
        Log.d(makeLogTag(tag), message);
    }

    public static void debug(@NonNull Class<?> c, @NonNull String message) {
        Log.d(makeLogTag(c), message);
    }

    public static void trace(@NonNull String tag, @Nullable String message) {
        trace(makeLogTag(tag), message, null);
    }

    public static void trace(@NonNull String tag, @Nullable Throwable throwable) {
        trace(makeLogTag(tag), "", throwable);
    }

    public static void trace(@NonNull Class<?> c, @Nullable String message) {
        trace(makeLogTag(c), message, null);
    }

    public static void trace(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        String fullMessage = getMessageForLog(message, throwable);
        Log.d(tag, fullMessage);
        Queue.addTrace(addCallTimeAndCurrentThread(buildMessage(tag, fullMessage)), SLF4J_LOGGER);
    }

    public static void info(@NonNull String tag, @Nullable String message) {
        info(makeLogTag(tag), message, null);
    }

    public static void info(@NonNull String tag, @Nullable Throwable throwable) {
        info(makeLogTag(tag), "", throwable);
    }

    public static void info(@NonNull Class<?> c, @Nullable String message) {
        info(makeLogTag(c), message, null);
    }

    public static void info(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        String fullMessage = getMessageForLog(message, throwable);
        Log.i(tag, fullMessage);
        Queue.addInfo(addCallTimeAndCurrentThread(buildMessage(tag, fullMessage)), SLF4J_LOGGER);
    }

    public static void warning(@NonNull String tag, @Nullable String message) {
        warning(makeLogTag(tag), message, null);
    }

    public static void warning(@NonNull String tag, @Nullable Throwable throwable) {
        warning(makeLogTag(tag), "", throwable);
    }

    public static void warning(@NonNull Class<?> c, @Nullable String message) {
        warning(makeLogTag(c), message, null);
    }

    public static void warning(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        String fullMessage = getMessageForLog(message, throwable);
        Log.w(tag, fullMessage);
        Queue.addWarn(addCallTimeAndCurrentThread(buildMessage(tag, fullMessage)), SLF4J_LOGGER);
    }

    public static void error(@NonNull Class<?> c, @Nullable String message) {
        error(makeLogTag(c), message);
    }

    public static void error(@NonNull Class<?> c, @Nullable Throwable throwable) {
        error(makeLogTag(c), "", throwable);
    }

    public static void error(@NonNull String tag, @Nullable String message) {
        error(tag, message, null);
    }

    public static void error(@NonNull String tag, @Nullable Throwable throwable) {
        error(tag, "", throwable);
    }

    public static void error(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {

        String fullMessage = getMessageForLog(message, throwable);

        Log.e(makeLogTag(tag), getMessageForLog(message, throwable));
        Queue.addError(addCallTimeAndCurrentThread(buildMessage(tag, fullMessage)), SLF4J_LOGGER);

    }

    private static String getMessageForLog(@Nullable String message, @Nullable Throwable throwable) {
        String out = message == null ? "null" : message;
        if (throwable != null)
            out = message + '\n' + Log.getStackTraceString(throwable);
        return out;
    }

////////////////////// logMethods - end //////////////////////


}