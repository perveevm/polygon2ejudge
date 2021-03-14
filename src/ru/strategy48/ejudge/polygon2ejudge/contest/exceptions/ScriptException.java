package ru.strategy48.ejudge.polygon2ejudge.contest.exceptions;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} thrown while executing scripts
 */
public class ScriptException extends ContestException {
    public ScriptException(final String cmd) {
        super("error happened while executing command (" + cmd + ")");
    }

    public ScriptException(final String cmd, final Throwable cause) {
        super("error happened while executing command (" + cmd + ")", cause);
    }
}
