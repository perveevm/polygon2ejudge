package ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} thrown if error happened while sending requests to Polygon server
 */
public class ConnectionError extends PolygonException {
    public ConnectionError(final String url) {
        super("error happened while sending request to URL: " + url);
    }

    public ConnectionError(final String url, final Throwable cause) {
        super("error happened while sending request to URL: " + url, cause);
    }
}
