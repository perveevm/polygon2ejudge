package ru.strategy48.ejudge.polygon2ejudge.contest.exceptions;

import java.nio.file.Path;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} thrown while parsing configuration files
 */
public class ConfigurationException extends ContestException {
    public ConfigurationException(final Path path) {
        super("error parsing configuration file (" + path.toString() + ")");
    }

    public ConfigurationException(final Path path, final Throwable cause) {
        super("error parsing configuration file (" + path.toString() + ")", cause);
    }
}
