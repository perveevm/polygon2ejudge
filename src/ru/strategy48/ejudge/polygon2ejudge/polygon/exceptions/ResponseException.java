package ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} thrown if given Polygon response is bad
 */
public class ResponseException extends PolygonException {
    public ResponseException(final String method) {
        super("error happened while getting response from API method " + method);
    }

    public ResponseException(final String method, final Throwable cause) {
        super("error happened while getting response from API method " + method, cause);
    }

    public ResponseException(final String message, final String json) {
        super("bad JSON response: " + json + " (" + message + ")");
    }
}
