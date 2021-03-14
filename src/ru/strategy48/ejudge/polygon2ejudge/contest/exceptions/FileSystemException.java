package ru.strategy48.ejudge.polygon2ejudge.contest.exceptions;

import java.nio.file.Path;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes {@link Exception} thrown while working with files (reading/writing/creating/moving/etc.)
 */
public class FileSystemException extends ContestException {
    public FileSystemException(final Path path) {
        super("error happened while reading/writing at path (" + path.toString() + ")");
    }

    public FileSystemException(final Path path, final Throwable cause) {
        super("error happened while reading/writing at path (" + path.toString() + ")", cause);
    }

    public FileSystemException(final Path from, final Path to) {
        super("error happened while copying/moving files at paths (" + from.toString() + " -> " + to.toString() + ")");
    }

    public FileSystemException(final Path from, final Path to, final Throwable cause) {
        super("error happened while copying/moving files at paths (" + from.toString() + " -> " + to.toString() + ")", cause);
    }
}
