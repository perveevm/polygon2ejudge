package ru.strategy48.ejudge.polygon2ejudge.contest.exceptions;

public class UnsupportedLanguageException extends ContestException {
    public UnsupportedLanguageException(final String type) {
        super("unsupported language: " + type);
    }

    public UnsupportedLanguageException(final String type, final Throwable cause) {
        super("unsupported language: " + type, cause);
    }
}
