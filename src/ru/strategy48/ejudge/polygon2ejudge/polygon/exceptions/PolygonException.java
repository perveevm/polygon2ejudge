package ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} throws while working with Polygon API
 */
public class PolygonException extends Exception {
    public PolygonException(final String message) {
        super("Error happened while working with Polygon: " + message);
    }

    public PolygonException(final String message, final Throwable cause) {
        super("Error happened while working with Polygon: " + message, cause);
    }
}
