package ru.strategy48.ejudge.polygon2ejudge;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides console logging methods. Based on {@link Logger}
 */
public class ConsoleLogger {
    private static final Logger logger = Logger.getLogger("Logger");

    /**
     * Prints given information to log
     *
     * @param message given message
     */
    public static void logInfo(final String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Prints given formatted information to log
     *
     * @param format formatting {@link String}
     * @param args   formatted arguments
     */
    public static void logInfo(final String format, final Object... args) {
        logInfo(String.format(format, args));
    }

    /**
     * Prints given error to log
     *
     * @param cause   {@link Throwable} that caused error
     * @param message given message
     */
    public static void logError(final Throwable cause, final String message) {
        logger.log(Level.WARNING, message, cause);
    }

    /**
     * Prints given formatted error message to log
     *
     * @param cause  {@link Throwable} that caused error
     * @param format formatting {@link String}
     * @param args   formatted arguments
     */
    public static void logError(final Throwable cause, final String format, final Object... args) {
        logError(cause, String.format(format, args));
    }
}
