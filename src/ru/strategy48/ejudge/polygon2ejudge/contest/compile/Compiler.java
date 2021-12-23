package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;

import java.nio.file.Path;

public interface Compiler {
    void compile(final Path sourcePath) throws ContestException;
}
