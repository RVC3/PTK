package ru.ppr.shtrih;

import android.support.annotation.NonNull;

import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

/**
 * Настройки логгера.
 *
 * @author Aleksandr Brazhkin
 */
public class LogbackConfig {

    private static final String LOGGER_NAME = "com.shtrih";
    private static final String FILE_MAME = "shtrih.log";
    private static final String ARCHIVE_PATTERN = "shtrih%i.log.zip";
    private static final String MAX_FILE_SIZE = "10MB";
    public static final int MAX_FILE_COUNT = 10;

    static void configure(@NonNull File logDir) {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger shtrihLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LOGGER_NAME);

        // reset the shtrihLogger (which may already have been initialized)
        // since we want to reconfigure it
        shtrihLogger.detachAndStopAllAppenders();

        // setup FileAppender
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder1.start();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(lc);
        rollingFileAppender.setFile(new File(logDir, FILE_MAME).getPath());

        FixedWindowRollingPolicy fixedWindowRollingPolicy = new FixedWindowRollingPolicy();
        fixedWindowRollingPolicy.setContext(lc);
        // rolling policies need to know their parent
        // it's one of the rare cases, where a sub-component knows about its parent
        fixedWindowRollingPolicy.setParent(rollingFileAppender);
        fixedWindowRollingPolicy.setMinIndex(1);
        fixedWindowRollingPolicy.setMaxIndex(MAX_FILE_COUNT);
        fixedWindowRollingPolicy.setFileNamePattern(new File(logDir, ARCHIVE_PATTERN).getPath());
        fixedWindowRollingPolicy.start();

        SizeBasedTriggeringPolicy<ILoggingEvent> sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy<>();
        sizeBasedTriggeringPolicy.setMaxFileSize(MAX_FILE_SIZE);
        sizeBasedTriggeringPolicy.start();

        rollingFileAppender.setEncoder(encoder1);
        rollingFileAppender.setRollingPolicy(fixedWindowRollingPolicy);
        rollingFileAppender.setTriggeringPolicy(sizeBasedTriggeringPolicy);
        rollingFileAppender.start();

        // SDK штриха пишет логи напрямую, убираем дублирование
//        // setup LogcatAppender
//        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
//        encoder2.setContext(lc);
//        encoder2.setPattern("%logger{12} [%thread] %msg%n");
//        encoder2.start();
//
//        LogcatAppender logcatAppender = new LogcatAppender();
//        logcatAppender.setContext(lc);
//        logcatAppender.setEncoder(encoder2);
//        logcatAppender.start();

        // add the newly created appenders to the shtrih logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        shtrihLogger.addAppender(rollingFileAppender);
//        shtrihLogger.addAppender(logcatAppender);
        shtrihLogger.setLevel(Level.TRACE);
    }
}
