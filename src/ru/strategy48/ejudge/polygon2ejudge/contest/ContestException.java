package ru.strategy48.ejudge.polygon2ejudge.contest;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} throwed while configuring contest
 */
public class ContestException extends Exception {
    public ContestException(final String message) {
        super("Error happened while working with contest files: " + message);
    }

    public ContestException(final String message, final Throwable cause) {
        super("Error happened while working with contest files: " + message, cause);
    }
}
