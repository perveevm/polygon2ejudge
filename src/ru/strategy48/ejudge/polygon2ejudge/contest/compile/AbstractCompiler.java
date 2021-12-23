package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ScriptException;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractCompiler implements Compiler {
    private final Path compileDirectory;
    private final String compilationScript;

    public AbstractCompiler(final Path compileDirectory, final String compilationScript) {
        this.compileDirectory = compileDirectory;
        this.compilationScript = compilationScript;
    }

    @Override
    public void compile(final Path sourcePath) throws ContestException {
        ConsoleLogger.logInfo(String.format("Compiling file via script line: \"%s\"", compilationScript));

        if (compilationScript != null) {
            try {
                Process compilation = Runtime.getRuntime().exec(compilationScript, null, compileDirectory.toFile());
                int exitCode = compilation.waitFor();
                if (exitCode != 0) {
                    throw new ScriptException(compilationScript);
                }
            } catch (IOException | InterruptedException e) {
                throw new ScriptException(compilationScript, e);
            }
        }

        prepareExecutable(sourcePath);
    }

    protected void prepareExecutable(final Path sourcePath) throws ContestException {
        // Do nothing, can override
    }
}
