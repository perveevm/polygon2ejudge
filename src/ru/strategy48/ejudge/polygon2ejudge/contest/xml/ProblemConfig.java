package ru.strategy48.ejudge.polygon2ejudge.contest.xml;

import ru.strategy48.ejudge.polygon2ejudge.contest.objects.Group;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.ProblemFile;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.Solution;
import ru.strategy48.ejudge.polygon2ejudge.contest.objects.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Class that represents problem.xml config from Polygon
 */
public class ProblemConfig {
    private final Map<String, String> names;

    private final int timeLimit;
    private final int memoryLimit;

    private final String inputFilePattern;
    private final String outputFilePattern;
    private final List<Test> tests;
    private final List<Group> groups;

    private final List<ProblemFile> resources;
    private final List<ProblemFile> executables;

    private final ProblemFile checker;
    private final List<ProblemFile> validators;
    private final List<Solution> solutions;
    private final ProblemFile interactor;

    public ProblemConfig(final Map<String, String> names,
                         final int timeLimit, final int memoryLimit,
                         final String inputFilePattern, final String outputFilePattern,
                         final List<Test> tests, final List<Group> groups,
                         final List<ProblemFile> resources, final List<ProblemFile> executables,
                         final ProblemFile checker, final List<ProblemFile> validators,
                         final List<Solution> solutions, final ProblemFile interactor) {
        this.names = names;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.inputFilePattern = inputFilePattern;
        this.outputFilePattern = outputFilePattern;
        this.tests = tests;
        this.groups = groups;
        this.resources = resources;
        this.executables = executables;
        this.checker = checker;
        this.validators = validators;
        this.solutions = solutions;
        this.interactor = interactor;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public String getInputFilePattern() {
        return inputFilePattern;
    }

    public String getOutputFilePattern() {
        return outputFilePattern;
    }

    public List<Test> getTests() {
        return tests;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<ProblemFile> getResources() {
        return resources;
    }

    public List<ProblemFile> getExecutables() {
        return executables;
    }

    public ProblemFile getChecker() {
        return checker;
    }

    public List<ProblemFile> getValidators() {
        return validators;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public ProblemFile getInteractor() {
        return interactor;
    }
}
