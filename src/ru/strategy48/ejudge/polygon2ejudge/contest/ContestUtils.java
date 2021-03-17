package ru.strategy48.ejudge.polygon2ejudge.contest;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.*;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.*;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        System.out.println("=== PREPARING SERVE.CFG ===");
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
        System.out.println("Done!");
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
                                      final String genericProblemName, final int ejudgeProblemId, final String shortName)
            throws PolygonException, ContestException {
        System.out.printf("=== PREPARING PROBLEM %s ===%n", shortName);

        int packageId = prepareArchive(session, problemId, problemDirectory);

        Path downloadedProblemDirectory = Paths.get(problemDirectory.toString(), String.valueOf(packageId));
        prepareExecutables(downloadedProblemDirectory);

        List<Test> tests = prepareTests(downloadedProblemDirectory);
        prepareAnswers(downloadedProblemDirectory, tests);

        List<Group> groups = generateGroups(downloadedProblemDirectory, tests);
        if (groups != null) {
            prepareGroups(downloadedProblemDirectory, groups);
        }

        prepareConfigFile(downloadedProblemDirectory, groups, tests, genericProblemName, ejudgeProblemId, shortName);

        cleanUp(downloadedProblemDirectory);
        System.out.println("Done!\n");
    }

    private static int prepareArchive(final PolygonSession session, final int problemId, final Path problemDirectory)
            throws PolygonException, ContestException {
        System.out.println("=== PREPARING ARCHIVE ===");
        System.out.printf("Getting packages list for problem %d\n", problemId);

        List<Package> packages = session.getProblemPackages(problemId);
        packages.sort(Comparator.comparingInt(Package::getId).reversed());

        int packageId = packages.get(0).getId();

        System.out.printf("Downloading package %d for problem %d to %s\n", packageId, problemId,
                problemDirectory.toString());
        Path archivePath = session.saveProblemPackageToFile(problemId, packageId, problemDirectory);

        System.out.println("Extracting archive");
        try {
            ZipFile zipFile = new ZipFile(archivePath.toFile());
            zipFile.extractAll(Paths.get(problemDirectory.toString(), String.valueOf(packageId)).toString());
        } catch (IOException e) {
            throw new FileSystemException(archivePath, e);
        }

        System.out.println("Deleting archive file");
        deleteFile(archivePath);

        return packageId;
    }

    private static void prepareExecutables(final Path problemDirectory) throws ContestException {
        System.out.println("=== PREPARING EXECUTABLES ===");
        Document document = getProblemXML(problemDirectory);

        NodeList resources = ((Element) ((Element) document.getElementsByTagName("files").item(0))
                .getElementsByTagName("resources").item(0)).getElementsByTagName("file");
        NodeList executables = ((Element) ((Element) document.getElementsByTagName("files").item(0))
                .getElementsByTagName("executables").item(0)).getElementsByTagName("executable");

        for (int i = 0; i < resources.getLength(); i++) {
            Element resource = (Element) resources.item(i);
            String curFile = resource.getAttribute("path");
            Path fromPath = Paths.get(problemDirectory.toString(), curFile);
            Path toPath = Paths.get(problemDirectory.getParent().toString(), fromPath.getFileName().toString());

            copyFile(fromPath, toPath);
        }

        for (int i = 0; i < executables.getLength(); i++) {
            Element executableSource = (Element) ((Element) executables.item(i))
                    .getElementsByTagName("source").item(0);
            String curFile = executableSource.getAttribute("path");
            String curType = executableSource.getAttribute("type");
            Path fromPath = Paths.get(problemDirectory.toString(), curFile);
            Path toPath = Paths.get(problemDirectory.getParent().toString(), fromPath.getFileName().toString());

            copyFile(fromPath, toPath);
            if (curType.contains("cpp")) {
                compileCode(toPath, true);
            }
        }
    }

    private static List<Test> prepareTests(final Path problemDirectory) throws ContestException {
        System.out.println("=== PREPARING TESTS ===");

        List<Test> allTests = new ArrayList<>();
        createDirectory(Paths.get(problemDirectory.getParent().toString(), "tests"));

        Document document = getProblemXML(problemDirectory);

        Element testset = (Element) ((Element) document.getElementsByTagName("judging").item(0))
                .getElementsByTagName("testset").item(0);
        NodeList tests = ((Element) testset.getElementsByTagName("tests").item(0)).getElementsByTagName("test");

        String inputFormat = (testset.getElementsByTagName("input-path-pattern").item(0)).getTextContent();
        int testCount = tests.getLength();

        String testNameFormat = "%02d";
        if (testCount >= 100) {
            testNameFormat = "%03d";
        }
        if (testCount >= 1000) {
            testNameFormat = "%04d";
        }

        for (int i = 0; i < tests.getLength(); i++) {
            Element test = (Element) tests.item(i);

            int group = test.hasAttribute("group") ? Integer.parseInt(test.getAttribute("group")) : -1;
            int points = test.hasAttribute("points") ? (int) Double.parseDouble(test.getAttribute("points")) : -1;
            boolean isSample = test.hasAttribute("sample") && Boolean.parseBoolean(test.getAttribute("sample"));
            boolean isGenerated = test.getAttribute("method").equals("generated");

            String fromFile = null;
            if (test.hasAttribute("from-file")) {
                fromFile = test.getAttribute("from-file");
            }

            allTests.add(new Test(i + 1, group, points, isSample));

            if (!isGenerated) {
                System.out.println("Copying manual test #" + (i + 1));

                Path from = Paths.get(problemDirectory.toString(), String.format(inputFormat, i + 1));
                Path to = Paths.get(problemDirectory.getParent().toString(), "tests",
                        String.format(testNameFormat, i + 1));

                copyFile(from, to);

                continue;
            }

            System.out.println("Generating test #" + (i + 1));

            Path testFile = createFile(Paths.get(problemDirectory.getParent().toString(), "tests",
                    String.format(testNameFormat, i + 1)));
            executeScript(test.getAttribute("cmd"), problemDirectory.getParent(), null, testFile);

            if (fromFile != null) {
                Path from = Paths.get(problemDirectory.getParent().toString(), fromFile);
                Path to = testFile.toAbsolutePath();

                deleteFile(to);
                moveFile(from, to);
            }
        }

        System.out.println("Deleting temporary files");
        for (int i = 0; i < tests.getLength(); i++) {
            Element test = (Element) tests.item(i);
            if (test.hasAttribute("from-file")) {
                String fromFile = test.getAttribute("from-file");

                deleteFile(Paths.get(problemDirectory.getParent().toString(), fromFile));
            }
        }

        return allTests;
    }

    private static void prepareAnswers(final Path problemDirectory, final List<Test> tests) throws ContestException {
        System.out.println("=== PREPARING TESTS ANSWERS ===");

        int testCount = tests.size();
        String testNameFormat = "%02d";
        if (testCount >= 100) {
            testNameFormat = "%03d";
        }
        if (testCount >= 1000) {
            testNameFormat = "%04d";
        }

        Document document = getProblemXML(problemDirectory);
        NodeList solutions = ((Element) ((Element) document.getElementsByTagName("assets").item(0))
                .getElementsByTagName("solutions").item(0)).getElementsByTagName("solution");
        Path checkerFrom = Paths.get(problemDirectory.toString(), ((Element) ((Element) ((Element) document.getElementsByTagName("assets").item(0)).
                getElementsByTagName("checker").item(0)).getElementsByTagName("source").item(0)).
                getAttribute("path"));
        String checkerType = ((Element) ((Element) ((Element) document.getElementsByTagName("assets").item(0)).
                getElementsByTagName("checker").item(0)).getElementsByTagName("source").item(0)).
                getAttribute("type");
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
        for (int i = 0; i < solutions.getLength(); i++) {
            Element solution = (Element) solutions.item(i);
            if (solution.getAttribute("tag").equals("main")) {
                solutionDir = ((Element) solution.getElementsByTagName("source").item(0))
                        .getAttribute("path");
                solutionType = ((Element) solution.getElementsByTagName("source").item(0))
                        .getAttribute("type");
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
            System.out.println("Generating answer for test #" + (i + 1));
            Path inputFile = Paths.get(testsDir.toString(), String.format(testNameFormat, i + 1));
            Path outputFile = Paths.get(testsDir.toString(), String.format(testNameFormat + ".a", i + 1));

            createFile(outputFile);
            executeScript(removeExtension(to.getFileName().toString()), problemDirectory.getParent(), inputFile, outputFile);
        }
    }

    private static List<Group> generateGroups(final Path problemDirectory, final List<Test> allTests)
            throws ContestException {
        System.out.println("=== PARSING GROUPS ===");
        Document document = getProblemXML(problemDirectory);

        Element testset = (Element) ((Element) document.getElementsByTagName("judging").item(0))
                .getElementsByTagName("testset").item(0);

        if (testset.getElementsByTagName("groups").getLength() == 0) {
            return null;
        }

        List<Group> allGroups = new ArrayList<>();
        NodeList groups = ((Element) testset.getElementsByTagName("groups").item(0)).getElementsByTagName("group");

        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);

            int id = Integer.parseInt(group.getAttribute("name"));
            List<Test> curTests = allTests.stream().filter((test) -> test.getGroup() == id).collect(Collectors.toList());
            List<Integer> dependencies = null;

            System.out.println("Parsing group #" + id);

            if (group.getElementsByTagName("dependencies").getLength() != 0) {
                NodeList dependenciesList = ((Element) group.getElementsByTagName("dependencies").item(0))
                        .getElementsByTagName("dependency");
                dependencies = new ArrayList<>();

                for (int j = 0; j < dependenciesList.getLength(); j++) {
                    Element dependency = (Element) dependenciesList.item(j);
                    dependencies.add(Integer.parseInt(dependency.getAttribute("group")));
                }
            }

            FeedbackPolicy feedbackPolicy = switch (group.getAttribute("feedback-policy")) {
                case "complete" -> FeedbackPolicy.COMPLETE;
                case "icpc" -> FeedbackPolicy.ICPC;
                case "points" -> FeedbackPolicy.POINTS;
                default -> FeedbackPolicy.NONE;
            };
            PointsPolicy pointsPolicy;
            if (group.getAttribute("points-policy").equals("each-test")) {
                pointsPolicy = PointsPolicy.EACH_TEST;
            } else {
                pointsPolicy = PointsPolicy.COMPLETE_GROUP;
            }

            allGroups.add(new Group(id, curTests, dependencies, feedbackPolicy, pointsPolicy));
        }

        return allGroups;
    }

    private static void prepareGroups(final Path problemDirectory, final List<Group> groups) throws ContestException {
        System.out.println("=== GENERATING VALUER.CFG ===");

        Path valuerPath = Paths.get(problemDirectory.getParent().toString(), "valuer.cfg");

        String res = "global {\n\tstat_to_users;\n}\n" +
                groups.stream().map(Group::toString).collect(Collectors.joining("\n"));
        writeFile(valuerPath, res);
    }

    private static void cleanUp(final Path problemDirectory) throws ContestException {
        System.out.println("=== CLEANING UP ===");

        try {
            FileUtils.deleteDirectory(problemDirectory.toFile());
        } catch (IOException e) {
            throw new FileSystemException(problemDirectory, e);
        }
    }

    private static void prepareConfigFile(final Path problemDirectory, final List<Group> allGroups,
                                          final List<Test> allTests, final String genericProblemName,
                                          final int ejudgeProblemId, final String shortName) throws ContestException {
        System.out.println("=== GENERATING PROBLEM.CFG ===");

        StringBuilder res = new StringBuilder();
        Document document = getProblemXML(problemDirectory);

        NodeList names = ((Element) document.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        Element testset = (Element) ((Element) document.getElementsByTagName("judging").item(0))
                .getElementsByTagName("testset").item(0);
        String checkerName = Paths.get(((Element) ((Element) ((Element) document.getElementsByTagName("assets").
                item(0)).getElementsByTagName("checker").item(0)).getElementsByTagName("source").
                item(0)).getAttribute("path")).getFileName().toString();
        String interactorName = null;
        if (((Element) document.getElementsByTagName("assets").item(0)).
                getElementsByTagName("interactor").getLength() != 0) {
            interactorName = Paths.get(((Element) ((Element) ((Element) document.getElementsByTagName("assets").
                    item(0)).getElementsByTagName("interactor").item(0)).getElementsByTagName("source").
                    item(0)).getAttribute("path")).getFileName().toString();
            interactorName = removeExtension(interactorName);
        }

        checkerName = removeExtension(checkerName);
        String longName = ((Element) names.item(0)).getAttribute("value");
        String testFormat = "%02d";
        if (allTests.size() >= 100) {
            testFormat = "%03d";
        }
        if (allTests.size() >= 1000) {
            testFormat = "%04d";
        }
        int timeLimit = Integer.parseInt(testset.getElementsByTagName("time-limit").item(0).getTextContent());
        int memoryLimit = Integer.parseInt(testset.getElementsByTagName("memory-limit").item(0).getTextContent());

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

        if (allTests.get(0).getPoints() != -1) {
            res.append(String.format("test_score_list = \"%s\"\n", allTests.stream().map(Test::getPoints).
                    map(Object::toString).collect(Collectors.joining(" "))));
        }

        if (allGroups == null) {
            res.append("valuer_cmd = \"\"\n");
        } else {
            List<String> visibility = new ArrayList<>();
            for (Group group : allGroups) {
                String curVisibility = switch (group.getFeedbackPolicy()) {
                    case ICPC, COMPLETE -> "brief";
                    case POINTS, NONE -> "hidden";
                };
                for (Interval interval : group.getTestsIntervals()) {
                    visibility.add(interval.toString() + ":" + curVisibility);
                }
            }
            res.append(String.format("open_tests = \"%s\"\n", String.join(",", visibility)));
            res.append(String.format("final_open_tests = \"1-%d:full\"\n", allTests.size()));
        }

        res.append("autoassign_variants = 0\n");
        res.append("normalization = \"nl\"\n");

        Path configPath = Paths.get(problemDirectory.getParent().toString(), "problem.cfg");
        writeFile(configPath, res.toString());
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

    private static String removeExtension(final String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private static void executeScript(final String cmd, final Path workingDirectory,
                                      final Path inputRedirection, final Path outputRedirection)
            throws ContestException {
        System.out.println("Executing " + cmd);

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
        System.out.println("Compiling " + sourcePath.getFileName());

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

    private static void copyFile(final Path from, final Path to) throws ContestException {
        System.out.printf("Copying %s to %s\n", from.toString(), to.toString());

        try {
            Files.copy(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    private static void moveFile(final Path from, final Path to) throws ContestException {
        System.out.printf("Moving %s to %s\n", from.toString(), to.toString());

        try {
            Files.move(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    private static void deleteFile(final Path path) throws ContestException {
        System.out.printf("Deleting %s\n", path.toString());

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    private static Path createFile(final Path path) throws ContestException {
        System.out.printf("Creating file %s\n", path.toString());

        try {
            return Files.createFile(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    private static void createDirectory(final Path path) throws ContestException {
        System.out.printf("Creating %s directory\n", path.toString());

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    private static String readFile(final Path path) throws ContestException {
        System.out.printf("Reading file %s\n", path.toString());

        StringBuilder res = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            char[] buffer = new char[1 << 16];
            int cnt;
            while ((cnt = reader.read(buffer)) != -1) {
                res.append(buffer, 0, cnt);
            }
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }

        return res.toString();
    }

    private static void writeFile(final Path path, final String data) throws ContestException {
        System.out.printf("Writing file %s\n", path.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(data);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }
}
