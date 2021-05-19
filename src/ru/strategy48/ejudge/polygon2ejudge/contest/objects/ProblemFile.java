package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

import java.nio.file.Path;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Class that represents resource and executable problem files
 */
public class ProblemFile {
    final Path path;
    final String type;

    public ProblemFile(final Path path, final String type) {
        this.path = path;
        this.type = type;
    }

    public ProblemFile(final Path path) {
        this.path = path;
        this.type = null;
    }

    public Path getPath() {
        return path;
    }

    public String getType() {
        return type;
    }
}
