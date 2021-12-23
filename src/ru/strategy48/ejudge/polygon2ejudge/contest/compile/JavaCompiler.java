package ru.strategy48.ejudge.polygon2ejudge.contest.compile;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JavaCompiler extends AbstractCompiler implements Compiler {
    private static JavaCompiler instance;

    private JavaCompiler() {
        super("javac -cp %s %s");
    }

    @Override
    public void compile(final Path sourcePath) throws ContestException {
        String className = findMainClassName(sourcePath);
        Path newSourcePath;
        if (!sourcePath.equals(Path.of(sourcePath.getParent().toString(), className + ".java"))) {
            newSourcePath = Path.of(sourcePath.getParent().toString(), className + ".java");
            FileUtils.copyFile(sourcePath, newSourcePath);
        } else {
            newSourcePath = sourcePath;
        }

        super.compile(newSourcePath, newSourcePath.getParent(), newSourcePath);
        prepareExecutable(newSourcePath);
    }

    public static JavaCompiler getInstance() {
        if (instance == null) {
            instance = new JavaCompiler();
        }
        return instance;
    }

    private void prepareExecutable(final Path sourcePath) throws ContestException {
        String script = "#!/bin/bash" + System.lineSeparator() +
                String.format("java -Xmx512M -Xss512M -DEJUDGE=true -Duser.language=en -Duser.region=US" +
                        "-Duser.variant=US %s \"$@\"", FileUtils.removeExtension(sourcePath)) + System.lineSeparator();
        FileUtils.writeFile(FileUtils.removeExtension(sourcePath), script);
        FileUtils.makeExecutable(FileUtils.removeExtension(sourcePath));
    }

    public String findMainClassName(final Path sourcePath) throws ContestException {
        String source = FileUtils.readFile(sourcePath);
        JavaParser parser = new JavaParser();
        Optional<CompilationUnit> cuOpt = parser.parse(source).getResult();
        if (cuOpt.isEmpty()) {
            throw new ContestException("Cannot compile file " + sourcePath);
        }
        CompilationUnit cu = cuOpt.get();

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            if (classDeclaration.isInterface() || classDeclaration.isInnerClass() || classDeclaration.isLocalClassDeclaration()) {
                continue;
            }

            List<MethodDeclaration> methods = classDeclaration.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration : methods) {
                if (methodDeclaration.getAccessSpecifier() != AccessSpecifier.PUBLIC) {
                    continue;
                }
                if (!methodDeclaration.isStatic()) {
                    continue;
                }
                if (!methodDeclaration.getType().toString().equals("void")) {
                    continue;
                }
                if (!methodDeclaration.getNameAsString().equals("main")) {
                    continue;
                }

                List<Parameter> parameters = methodDeclaration.getParameters();
                if (parameters.size() != 1) {
                    continue;
                }
                if (!parameters.get(0).getType().toString().equals("String[]")) {
                    continue;
                }

                return classDeclaration.getNameAsString();
            }
        }

        return null;
    }
}
