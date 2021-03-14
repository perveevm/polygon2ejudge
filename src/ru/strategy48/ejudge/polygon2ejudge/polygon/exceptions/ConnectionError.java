package ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions;

public class ConnectionError extends PolygonException {
    public ConnectionError(final String url) {
        super("error happened while sending request to URL: " + url);
    }

    public ConnectionError(final String url, final Throwable cause) {
        super("error happened while sending request to URL: " + url, cause);
    }
}
