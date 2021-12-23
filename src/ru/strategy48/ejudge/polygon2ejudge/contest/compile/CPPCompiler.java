package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;

import java.nio.file.Path;

public class CPPCompiler extends AbstractCompiler implements Compiler {
    private static CPPCompiler instance;

    private CPPCompiler() {
        super("g++ -o %s %s -std=c++17 -O2 -DEJUDGE");
    }

    @Override
    public void compile(Path sourcePath) throws ContestException {
        super.compile(sourcePath, FileUtils.removeExtension(sourcePath), sourcePath);
    }

    public static CPPCompiler getInstance() {
        if (instance == null) {
            instance = new CPPCompiler();
        }
        return instance;
    }
}
