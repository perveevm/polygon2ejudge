package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils;

import java.nio.file.Path;

public class CPPCompiler extends AbstractCompiler {
    public CPPCompiler(final Path sourcePath) {
        super(sourcePath.getParent(), String.format("g++ -o %s %s -std=c++17 -O2 -DEJUDGE",
                FileUtils.removeExtension(sourcePath), sourcePath));
    }
}
