package ru.strategy48.ejudge.polygon2ejudge.contest;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.*;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.*;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.Test;
import ru.strategy48.ejudge.polygon2ejudge.contest.xml.*;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Package;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.PolygonException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.PolygonSession;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Problem;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static ru.strategy48.ejudge.polygon2ejudge.contest.FileUtils.*;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides methods for configuring problems and contests
 */
public class ContestUtils {
    /**
     * Prepares contest: prepares all problems and generates serve.cfg
     *
     * @param session            Polygon session for API usage
     * @param contestId          contest ID in Polygon
     * @param contestDirectory   contest directory (there will be created <code>problems</code> directory in it)
     * @param genericProblemName generic problem name in default config file
     * @param defaultConfig      default serve.cfg config path
     * @throws PolygonException if error happened while working with Polygon API
     * @throws ContestException if error happened while preparing contest
     */
    public static void prepareContest(final PolygonSession session, final int contestId, final Path contestDirectory,
                                      final String genericProblemName, final Path defaultConfig)
            throws PolygonException, ContestException {
        List<Problem> problems = session.getContestProblems(contestId);
        int ejudgeProblemId = 0;

        Path problemsDirectory = Paths.get(contestDirectory.toString(), "problems");
        if (Files.notExists(problemsDirectory)) {
            createDirectory(problemsDirectory);
        }

        for (Problem problem : problems) {
            ejudgeProblemId++;
            Path problemPath = Paths.get(problemsDirectory.toString(), problem.getName());
            createDirectory(problemPath);

            prepareProblem(session, problem.getId(), problemPath, genericProblemName, ejudgeProblemId,
                    String.valueOf((char) ('A' + ejudgeProblemId - 1)));
        }

        Path configDirectory = Paths.get(contestDirectory.toString(), "conf");
        if (Files.notExists(configDirectory)) {
            createDirectory(configDirectory);
        }

        ConsoleLogger.logInfo("=== PREPARING SERVE.CFG ===");
        Path configPath = Paths.get(configDirectory.toString(), "serve.cfg");
        if (Files.exists(configPath)) {
            deleteFile(Paths.get(configDirectory.toString(), "serve.cfg.old"));
            moveFile(configPath, Paths.get(configDirectory.toString(), "serve.cfg.old"));
        }

        StringBuilder serveCfg = new StringBuilder();
        serveCfg.append(readFile(defaultConfig)).append("\n");

        for (Problem problem : problems) {
            Path problemCfgPath = Paths.get(problemsDirectory.toString(), problem.getName(), "problem.cfg");
            serveCfg.append(readFile(problemCfgPath)).append("\n");
        }

        writeFile(configPath, serveCfg.toString());
        ConsoleLogger.logInfo("Everything is done!");
    }

    /**
     * Prepares problem: compiles executables, generates tests and answers, generates valuer.cfg and problem.cfg
     *
     * @param session            Polygon session
     * @param problemId          problem ID in Polygon
     * @param problemDirectory   problem directory (there will be created <code>tests</code> directory and other files)
     * @param genericProblemName generic problem name in default eJudge config
     * @param ejudgeProblemId    problem ID in contest
     * @param shortName          short problem name (for example A, B, C, etc.)
     * @throws PolygonException if error happened while working with Polygon API
     * @throws ContestException if error happened while preparing problem
     */
    public static void prepareProblem(final PolygonSession session, final int problemId, final Path problemDirectory,
                                      final String genericProblemName, final int ejudgeProblemId,
                                      final String shortName)
            throws PolygonException, ContestException {
        ConsoleLogger.logInfo("=== PREPARING PROBLEM %s ===%n", shortName);

        int packageId = prepareArchive(session, problemId, problemDirectory);

        Path downloadedProblemDirectory = Paths.get(problemDirectory.toString(), String.valueOf(packageId));

        ProblemConfig config = getProblemConfig(downloadedProblemDirectory);
        prepareExecutables(downloadedProblemDirectory, config);

        prepareTests(downloadedProblemDirectory, config);
        prepareAnswers(downloadedProblemDirectory, config);

        if (config.getGroups() != null) {
            prepareGroups(downloadedProblemDirectory, config);
        }

        prepareConfigFile(downloadedProblemDirectory, genericProblemName, ejudgeProblemId, shortName, config);

        cleanUp(downloadedProblemDirectory);
        ConsoleLogger.logInfo("Problem %s is done!", shortName);
    }

    private static int prepareArchive(final PolygonSession session, final int problemId, final Path problemDirectory)
            throws PolygonException, ContestException {
        ConsoleLogger.logInfo("=== PREPARING ARCHIVE ===");

        ConsoleLogger.logInfo("Getting packages list for problem %d\n", problemId);
        List<Package> packages = session.getProblemPackages(problemId);
        packages.sort(Comparator.comparingInt(Package::getId).reversed());

        int packageId = packages.get(0).getId();

        ConsoleLogger.logInfo("Downloading package %d for problem %d to %s\n", packageId, problemId,
                problemDirectory.toString());
        Path archivePath = session.saveProblemPackageToFile(problemId, packageId, problemDirectory);

        ConsoleLogger.logInfo("Extracting archive");
        try {
            ZipFile zipFile = new ZipFile(archivePath.toFile());
            zipFile.extractAll(Paths.get(problemDirectory.toString(), String.valueOf(packageId)).toString());
        } catch (IOException e) {
            throw new FileSystemException(archivePath, e);
        }

        ConsoleLogger.logInfo("Deleting archive file");
        deleteFile(archivePath);

        return packageId;
    }

    private static void prepareExecutables(final Path problemDirectory, final ProblemConfig config)
            throws ContestException {
        ConsoleLogger.logInfo("=== PREPARING EXECUTABLES ===");

        for (ProblemFile resource : config.getResources()) {
            Path fromPath = Paths.get(problemDirectory.toString(), resource.getPath().toString());
            Path toPath = Paths.get(problemDirectory.getParent().toString(), fromPath.getFileName().toString());
            copyFile(fromPath, toPath);
        }

        for (ProblemFile executable : config.getExecutables()) {
            Path fromPath = Paths.get(problemDirectory.toString(), executable.getPath().toString());
            Path toPath = Paths.get(problemDirectory.getParent().toString(), fromPath.getFileName().toString());
            copyFile(fromPath, toPath);

            if (executable.getType().contains("cpp")) {
                compileCode(toPath, true);
            }
        }
    }

    private static void prepareTests(final Path problemDirectory, final ProblemConfig config)
            throws ContestException {
        ConsoleLogger.logInfo("=== PREPARING TESTS ===");

        createDirectory(Paths.get(problemDirectory.getParent().toString(), "tests"));

        String inputFormat = config.getInputFilePattern();
        int testCount = config.getTests().size();

        String testNameFormat = getIntegerFormat(testCount);
        Set<String> executedMultigenScripts = new HashSet<>();
        for (int i = 0; i < config.getTests().size(); i++) {
            if (config.getTests().get(i).getMethod().equals(GenerationMethod.MANUAL)) {
                ConsoleLogger.logInfo("Copying manual test #" + (i + 1));

                Path from = Paths.get(problemDirectory.toString(), String.format(inputFormat, i + 1));
                Path to = Paths.get(problemDirectory.getParent().toString(), "tests",
                        String.format(testNameFormat, i + 1));
                copyFileCorrectingLineBreaks(from, to);

                continue;
            }

            ConsoleLogger.logInfo("Generating test #" + (i + 1));

            Path testFile = createFile(Paths.get(problemDirectory.getParent().toString(), "tests",
                    String.format(testNameFormat, i + 1)));

            if (!executedMultigenScripts.contains(config.getTests().get(i).getCmd())) {
                executeScript(config.getTests().get(i).getCmd(), problemDirectory.getParent(), null, testFile);
                executedMultigenScripts.add(config.getTests().get(i).getCmd());
            }

            String fromFile = config.getTests().get(i).getFromFile();
            if (fromFile != null) {
                Path from = Paths.get(problemDirectory.getParent().toString(), fromFile);
                Path to = testFile.toAbsolutePath();

                deleteFile(to);
                moveFile(from, to);
            }
        }

        ConsoleLogger.logInfo("=== VALIDATING TESTS ===");
        for (int i = 0; i < testCount; i++) {
            ConsoleLogger.logInfo("Validating test number %d", i + 1);
            Path testPath = Paths.get(problemDirectory.getParent().toString(), "tests",
                    String.format(testNameFormat, i + 1));

            for (ProblemFile validator : config.getValidators()) {
                String validatorFileName = removeExtension(validator.getPath().getFileName().toString());
                executeScript(validatorFileName, problemDirectory.getParent(), testPath, null);
            }
        }

        ConsoleLogger.logInfo("Deleting temporary files");
        for (int i = 0; i < testCount; i++) {
            String fromFile = config.getTests().get(i).getFromFile();
            if (fromFile != null) {
                deleteFile(Paths.get(problemDirectory.getParent().toString(), fromFile));
            }
        }
    }

    private static void prepareAnswers(final Path problemDirectory, final ProblemConfig config)
            throws ContestException {
        ConsoleLogger.logInfo("=== PREPARING TESTS ANSWERS ===");

        int testCount = config.getTests().size();
        String testNameFormat = getIntegerFormat(testCount);

        Path checkerFrom = Paths.get(problemDirectory.toString(), config.getChecker().getPath().toString());
        String checkerType = config.getChecker().getType();

        String interactorName = null;
        if (config.getInteractor() != null) {
            interactorName = removeExtension(config.getInteractor().getPath().getFileName().toString());
        }

        Path checkerTo = Paths.get(problemDirectory.getParent().toString(), checkerFrom.getFileName().toString());
        String fileWithoutExtension = removeExtension(checkerTo.getFileName().toString());

        deleteFile(checkerTo);
        deleteFile(Paths.get(checkerTo.getParent().toString(), fileWithoutExtension));
        copyFile(checkerFrom, checkerTo);

        if (checkerType.contains("cpp")) {
            compileCode(checkerTo, true);
        } else {
            throw new UnsupportedLanguageException(checkerType);
        }

        String solutionDir = null;
        String solutionType = "";
        for (int i = 0; i < config.getSolutions().size(); i++) {
            Solution solution = config.getSolutions().get(i);
            if (solution.getTag().equals("main")) {
                solutionDir = solution.getFile().getPath().toString();
                solutionType = solution.getFile().getType();
                break;
            }
        }

        Path from = Paths.get(problemDirectory.toString(), solutionDir);
        Path to = Paths.get(problemDirectory.getParent().toString(), from.getFileName().toString());

        copyFile(from, to);
        if (solutionType.contains("cpp")) {
            compileCode(to, false);
        } else {
            throw new UnsupportedLanguageException(solutionType);
        }

        Path testsDir = Paths.get(problemDirectory.getParent().toString(), "tests");
        for (int i = 0; i < testCount; i++) {
            ConsoleLogger.logInfo("Generating answer for test #" + (i + 1));
            Path inputFile = Paths.get(testsDir.toString(), String.format(testNameFormat, i + 1));
            Path outputFile = Paths.get(testsDir.toString(), String.format(testNameFormat + ".a", i + 1));

            createFile(outputFile);

            if (interactorName == null) {
                executeScript(removeExtension(to.getFileName().toString()), problemDirectory.getParent(), inputFile, outputFile);
            } else {
                executeScript(String.format("java -Xmx512M -Xss64M -DONLINE_JUDGE=true -Duser.language=en -Duser.region=US -Duser.variant=US -jar %s/files/CrossRun.jar \"%s tests/%s tests/%s\" \"%s\"", problemDirectory.getFileName().toString(), interactorName, inputFile.getFileName(), outputFile.getFileName(), removeExtension(to.getFileName().toString())), problemDirectory.getParent(), null, null);
            }
        }
    }

    private static void prepareGroups(final Path problemDirectory, final ProblemConfig config) throws ContestException {
        ConsoleLogger.logInfo("=== GENERATING VALUER.CFG ===");

        Path valuerPath = Paths.get(problemDirectory.getParent().toString(), "valuer.cfg");

        String res = "global {\n\tstat_to_users;\n}\n" +
                config.getGroups().stream().map(Group::toString).collect(Collectors.joining("\n"));
        writeFile(valuerPath, res);
    }

    private static void cleanUp(final Path problemDirectory) throws ContestException {
        ConsoleLogger.logInfo("=== CLEANING UP ===");

        try {
            FileUtils.deleteDirectory(problemDirectory.toFile());
        } catch (IOException e) {
            throw new FileSystemException(problemDirectory, e);
        }
    }

    private static void prepareConfigFile(final Path problemDirectory, final String genericProblemName,
                                          final int ejudgeProblemId, final String shortName,
                                          final ProblemConfig config)
            throws ContestException {
        ConsoleLogger.logInfo("=== GENERATING PROBLEM.CFG ===");

        StringBuilder res = new StringBuilder();

        String checkerName = removeExtension(config.getChecker().getPath().getFileName().toString());
        String interactorName = null;
        if (config.getInteractor() != null) {
            interactorName = removeExtension(config.getInteractor().getPath().getFileName().toString());
        }

        String longName = config.getNames().get("russian");
        String testFormat = getIntegerFormat(config.getTests().size());
        int timeLimit = config.getTimeLimit();
        int memoryLimit = config.getMemoryLimit();

        res.append("[problem]\n");
        res.append(String.format("id = %d\n", ejudgeProblemId));
        res.append(String.format("super = \"%s\"\n", genericProblemName));
        res.append(String.format("short_name = \"%s\"\n", shortName));
        res.append(String.format("long_name = \"%s\"\n", longName));
        res.append(String.format("internal_name = \"%s\"\n", problemDirectory.getParent().getFileName()));
        res.append("type = \"standard\"\n");
        res.append("test_dir = \"\"\n");
        res.append(String.format("test_pat = \"%s\"\n", testFormat));
        res.append("corr_dir = \"\"\n");
        res.append(String.format("corr_pat = \"%s.a\"\n", testFormat));
        res.append("info_dir = \"\"\n");

        if (timeLimit % 1000 == 0) {
            res.append(String.format("time_limit = %d\n", timeLimit / 1000));
        } else {
            res.append(String.format("time_limit_millis = %d\n", timeLimit));
        }
        res.append(String.format("real_time_limit = %d\n", (2 * timeLimit + 999) / 1000));

        char suffix = 'B';
        if (memoryLimit % 1024 == 0) {
            suffix = 'K';
            memoryLimit /= 1024;
        }
        if (memoryLimit % 1024 == 0) {
            suffix = 'M';
            memoryLimit /= 1024;
        }
        if (memoryLimit % 1024 == 0) {
            suffix = 'G';
            memoryLimit /= 1024;
        }
        res.append(String.format("max_vm_size = %d%c\n", memoryLimit, suffix));

        res.append("standard_checker = \"\"\n");
        res.append(String.format("check_cmd = \"%s\"\n", checkerName));

        if (interactorName != null) {
            res.append(String.format("interactor_cmd = \"%s\"\n", interactorName));
        }

        if (config.getTests().get(0).getPoints() != -1) {
            res.append(String.format("test_score_list = \"%s\"\n", config.getTests().stream().map(Test::getPoints).
                    map(Object::toString).collect(Collectors.joining(" "))));
        }

        if (config.getGroups() == null) {
            res.append("valuer_cmd = \"\"\n");
        } else {
            List<String> visibility = new ArrayList<>();
            for (Group group : config.getGroups()) {
                String curVisibility = switch (group.getFeedbackPolicy()) {
                    case ICPC, COMPLETE -> "brief";
                    case POINTS, NONE -> "hidden";
                };
                for (Interval interval : group.getTestsIntervals()) {
                    visibility.add(interval.toString() + ":" + curVisibility);
                }
            }
            res.append(String.format("open_tests = \"%s\"\n", String.join(",", visibility)));
            res.append(String.format("final_open_tests = \"1-%d:full\"\n", config.getTests().size()));
        }

        res.append("autoassign_variants = 0\n");
        res.append("normalization = \"nl\"\n");

        Path configPath = Paths.get(problemDirectory.getParent().toString(), "problem.cfg");
        writeFile(configPath, res.toString());
    }

    private static ProblemConfig getProblemConfig(final Path problemDirectory) throws ContestException {
        return XMLUtils.parseProblemXML(Paths.get(problemDirectory.toString(), "problem.xml"));
    }

    private static Document getProblemXML(final Path problemDirectory) throws ContestException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(Paths.get(problemDirectory.toString(), "problem.xml").toFile());
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigurationException(Paths.get(problemDirectory.toString(), "problem.xml"), e);
        }
    }

    private static String getIntegerFormat(final int cnt) {
        return "%0" + Math.max(String.valueOf(cnt).length(), 2) + "d";
    }

    private static String removeExtension(final String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private static void executeScript(final String cmd, final Path workingDirectory,
                                      final Path inputRedirection, final Path outputRedirection)
            throws ContestException {
        ConsoleLogger.logInfo("Executing script: \"%s\"", cmd);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(List.of("bash", "-c", cmd));
        processBuilder.directory(workingDirectory.toFile());

        if (inputRedirection != null) {
            processBuilder.redirectInput(inputRedirection.toFile());
        }
        if (outputRedirection != null) {
            processBuilder.redirectOutput(outputRedirection.toFile());
        }
        processBuilder.environment().put("PATH", processBuilder.environment().get("PATH") + ":"
                + workingDirectory.toString());

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new ScriptException(cmd);
            }
        } catch (IOException | InterruptedException e) {
            throw new ScriptException(cmd, e);
        }
    }

    private static void compileCode(final Path sourcePath, final boolean isScript) throws ContestException {
        ConsoleLogger.logInfo("Compiling file %s", sourcePath.getFileName());

        String command = String.format("g++ -o %s %s -std=c++17",
                removeExtension(sourcePath.getFileName().toString()),
                sourcePath.getFileName().toString());
        if (!isScript) {
            command += " -O2";
        } else {
            command += " -DEJUDGE";
        }

        try {
            Process compilation = Runtime.getRuntime().exec(command, null, sourcePath.getParent().toFile());
            int exitCode = compilation.waitFor();
            if (exitCode != 0) {
                throw new ScriptException(command);
            }
        } catch (IOException | InterruptedException e) {
            throw new ScriptException(command, e);
        }
    }
}
