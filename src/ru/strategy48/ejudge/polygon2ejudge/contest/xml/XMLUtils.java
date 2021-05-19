package ru.strategy48.ejudge.polygon2ejudge.contest.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ConfigurationException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides methods for parsing problem.xml config file to {@link ProblemConfig} class
 */
public class XMLUtils {
    public static ProblemConfig parseProblemXML(final Path configPath) throws ContestException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(configPath.toFile());
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConfigurationException(configPath, e);
        }

        Map<String, String> names = new HashMap<>();
        NodeList namesNode = ((Element) document.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        for (int i = 0; i < namesNode.getLength(); i++) {
            String curLang = ((Element) namesNode.item(i)).getAttribute("language");
            String curName = ((Element) namesNode.item(i)).getAttribute("value");
            names.put(curLang, curName);
        }

        Element testset = (Element) ((Element) document.getElementsByTagName("judging").item(0)).
                getElementsByTagName("testset").item(0);

        int timeLimit = Integer.parseInt(testset.getElementsByTagName("time-limit").item(0).getTextContent());
        int memoryLimit = Integer.parseInt(testset.getElementsByTagName("memory-limit").item(0).getTextContent());

        String inputFilePattern = testset.getElementsByTagName("input-path-pattern").item(0).getTextContent();
        String outputFilePattern = testset.getElementsByTagName("answer-path-pattern").item(0).getTextContent();

        int testCount = Integer.parseInt(testset.getElementsByTagName("test-count").item(0).getTextContent());

        List<Test> tests = new ArrayList<>(testCount);
        NodeList testsNode = ((Element) testset.getElementsByTagName("tests").item(0)).getElementsByTagName("test");
        for (int i = 0; i < testCount; i++) {
            String curMethodStr = ((Element) testsNode.item(i)).getAttribute("method");
            GenerationMethod method;
            switch (curMethodStr) {
                case "manual" -> method = GenerationMethod.MANUAL;
                case "generated" -> method = GenerationMethod.GENERATED;
                default -> throw new ContestException(String.format("Unknown test type: %s", curMethodStr));
            }

            String cmd = null;
            if (((Element) testsNode.item(i)).hasAttribute("cmd")) {
                cmd = ((Element) testsNode.item(i)).getAttribute("cmd");
            }
            int points = -1;
            if (((Element) testsNode.item(i)).hasAttribute("points")) {
                points = (int) Double.parseDouble(((Element) testsNode.item(i)).getAttribute("points"));
            }
            int group = -1;
            if (((Element) testsNode.item(i)).hasAttribute("group")) {
                points = Integer.parseInt(((Element) testsNode.item(i)).getAttribute("group"));
            }
            boolean sample = false;
            if (((Element) testsNode.item(i)).hasAttribute("sample")) {
                sample = Boolean.parseBoolean(((Element) testsNode.item(i)).getAttribute("sample"));
            }
            String fromFile = null;
            if (((Element) testsNode.item(i)).hasAttribute("from-file")) {
                fromFile = ((Element) testsNode.item(i)).getAttribute("from-file");
            }

            tests.add(new Test(i + 1, method, cmd, group, points, sample, fromFile));
        }

        NodeList resourcesNode = ((Element) ((Element) document.getElementsByTagName("files").item(0)).
                getElementsByTagName("resources").item(0)).getElementsByTagName("file");
        NodeList executablesNode = ((Element) ((Element) document.getElementsByTagName("files").item(0)).
                getElementsByTagName("executables").item(0)).getElementsByTagName("executable");

        List<ProblemFile> resources = new ArrayList<>(resourcesNode.getLength());
        List<ProblemFile> executables = new ArrayList<>(executablesNode.getLength());

        for (int i = 0; i < resourcesNode.getLength(); i++) {
            Path path = Path.of(((Element) resourcesNode.item(i)).getAttribute("path"));
            String type = null;
            if (((Element) resourcesNode.item(i)).hasAttribute("type")) {
                type = ((Element) resourcesNode.item(i)).getAttribute("type");
            }
            resources.add(new ProblemFile(path, type));
        }

        for (int i = 0; i < executablesNode.getLength(); i++) {
            executables.add(parseProblemFileFromNode((Element) executablesNode.item(i)));
        }

        Element assetsNode = (Element) document.getElementsByTagName("assets").item(0);
        Element checkerNode = (Element) assetsNode.getElementsByTagName("checker").item(0);
        NodeList validatorsNode = ((Element) assetsNode.getElementsByTagName("validators").item(0)).
                getElementsByTagName("validator");
        NodeList solutionsNode = ((Element) assetsNode.getElementsByTagName("solutions").item(0))
                .getElementsByTagName("solution");
        Element interactorNode = (Element) assetsNode.getElementsByTagName("interactor").item(0);

        Element checkerSource = (Element) checkerNode.getElementsByTagName("source").item(0);
        Path checkerPath = Path.of(checkerSource.getAttribute("path"));
        String checkerType = checkerSource.getAttribute("type");
        ProblemFile checker = new ProblemFile(checkerPath, checkerType);

        List<ProblemFile> validators = new ArrayList<>(validatorsNode.getLength());
        List<Solution> solutions = new ArrayList<>(solutionsNode.getLength());

        for (int i = 0; i < validatorsNode.getLength(); i++) {
            validators.add(parseProblemFileFromNode((Element) validatorsNode.item(i)));
        }

        for (int i = 0; i < solutionsNode.getLength(); i++) {
            String tag = ((Element) solutionsNode.item(i)).getAttribute("tag");
            solutions.add(new Solution(tag, parseProblemFileFromNode((Element) solutionsNode.item(i))));
        }

        Element interactorSource = (Element) interactorNode.getElementsByTagName("source").item(0);
        ProblemFile interactor = parseProblemFileFromNode(interactorSource);

        List<Group> groups = null;
        if (testset.getElementsByTagName("groups").getLength() != 0) {
            NodeList groupsNode = ((Element) testset.getElementsByTagName("groups").item(0)).
                    getElementsByTagName("group");
            groups = new ArrayList<>(groupsNode.getLength());

            for (int i = 0; i < groupsNode.getLength(); i++) {
                Element group = (Element) groupsNode.item(i);

                int id = Integer.parseInt(group.getAttribute("name"));
                List<Test> curTests = tests.stream().filter((test) -> test.getGroup() == id).
                        collect(Collectors.toList());

                List<Integer> dependencies = null;
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

                groups.add(new Group(id, curTests, dependencies, feedbackPolicy, pointsPolicy));
            }
        }

        return new ProblemConfig(names, timeLimit, memoryLimit, inputFilePattern, outputFilePattern,
                tests, groups, resources, executables, checker, validators, solutions, interactor);
    }

    private static ProblemFile parseProblemFileFromNode(final Element node) {
        Element source = (Element) node.getElementsByTagName("source").item(0);
        Path path = Path.of(source.getAttribute("path"));
        if (source.hasAttribute("type")) {
            return new ProblemFile(path, source.getAttribute("type"));
        } else {
            return new ProblemFile(path);
        }
    }
}
