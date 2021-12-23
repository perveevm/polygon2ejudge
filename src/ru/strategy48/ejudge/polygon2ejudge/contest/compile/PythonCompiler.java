package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;

import java.nio.file.Path;

public class PythonCompiler extends AbstractCompiler implements Compiler {
    private static PythonCompiler instance;

    private PythonCompiler() {
        super(null);
    }

    @Override
    public void compile(final Path sourcePath) throws ContestException {
        super.compile(sourcePath);
        prepareExecutable(sourcePath);
    }

    public static PythonCompiler getInstance() {
        if (instance == null) {
            instance = new PythonCompiler();
        }
        return instance;
    }

    protected void prepareExecutable(Path sourcePath) throws ContestException {
        String script = "#!/bin/bash" + System.lineSeparator() +
                String.format("/usr/bin/python3.9 %s \"$@\"", sourcePath) + System.lineSeparator();
        FileUtils.writeFile(FileUtils.removeExtension(sourcePath), script);
        FileUtils.makeExecutable(FileUtils.removeExtension(sourcePath));
    }
}
