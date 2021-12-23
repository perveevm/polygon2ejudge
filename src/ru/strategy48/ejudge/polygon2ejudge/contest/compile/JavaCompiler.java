package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;

import java.nio.file.Path;

public class JavaCompiler extends AbstractCompiler {
    public JavaCompiler(final Path sourcePath) {
        super(sourcePath.getParent(), String.format("javac -cp %s %s", sourcePath.getParent(), sourcePath));
    }

    @Override
    protected final void prepareExecutable(final Path sourcePath) throws ContestException {
        String script = "#!/bin/bash" + System.lineSeparator() +
                String.format("java -Xmx512M -Xss512M -DEJUDGE=true -Duser.language=en -Duser.region=US" +
                        "-Duser.variant=US %s \"$@\"", FileUtils.removeExtension(sourcePath)) + System.lineSeparator();
        FileUtils.writeFile(FileUtils.removeExtension(sourcePath), script);
        FileUtils.makeExecutable(FileUtils.removeExtension(sourcePath));
    }
}
