package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ScriptException;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractCompiler {
    private final String compilationScriptFormat;

    public AbstractCompiler(final String compilationScriptFormat) {
        this.compilationScriptFormat = compilationScriptFormat;
    }

    public final void compile(Path sourcePath, Object... args) throws ContestException {
        if (compilationScriptFormat == null) {
            ConsoleLogger.logInfo("Skipping compilation stage...");
            return;
        }

        String compilationScript = String.format(compilationScriptFormat, args);
        Path compileDirectory = sourcePath.getParent();
        ConsoleLogger.logInfo(String.format("Compiling file via script line: \"%s\"", compilationScript));

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
}
