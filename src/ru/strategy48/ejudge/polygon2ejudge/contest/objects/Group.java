package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes group of tests
 */
public class Group {
    private final String id;
    private final List<Test> tests;
    private final List<Integer> dependencies;
    private final FeedbackPolicy feedbackPolicy;
    private final PointsPolicy pointsPolicy;
    private final int score;

    private final List<Interval> testsIntervals = new ArrayList<>();

    /**
     * Constructs group from parameters provided by Polygon
     *
     * @param id             given group ID
     * @param tests          {@link List} of tests in given group
     * @param dependencies   {@link List} of dependencies for given group
     * @param feedbackPolicy feedback policy for given group
     * @param pointsPolicy   scoring policy for given group
     */
    public Group(final String id, final List<Test> tests, final List<Integer> dependencies,
                 final FeedbackPolicy feedbackPolicy, final PointsPolicy pointsPolicy) {
        this.id = id;
        this.tests = tests;
        this.dependencies = dependencies;
        this.feedbackPolicy = feedbackPolicy;
        this.pointsPolicy = pointsPolicy;
        this.score = tests.stream().map(Test::getPoints).reduce(0, Integer::sum);

        this.tests.sort(Comparator.comparing(Test::getId));

        int prev = this.tests.get(0).getId();
        for (int i = 1; i < this.tests.size(); i++) {
            if (this.tests.get(i).getId() != this.tests.get(i - 1).getId() + 1) {
                this.testsIntervals.add(new Interval(prev, this.tests.get(i).getId()));
                prev = this.tests.get(i).getId();
            }
        }

        this.testsIntervals.add(new Interval(prev, this.tests.get(this.tests.size() - 1).getId()));
    }

    public String getId() {
        return id;
    }

    public List<Test> getTests() {
        return tests;
    }

    public List<Integer> getDependencies() {
        return dependencies;
    }

    public FeedbackPolicy getFeedbackPolicy() {
        return feedbackPolicy;
    }

    public PointsPolicy getPointsPolicy() {
        return pointsPolicy;
    }

    public List<Interval> getTestsIntervals() {
        return testsIntervals;
    }

    /**
     * Transforms group to eJudge gvaluer-readable format
     *
     * @return {@link String} in gvaluer-readable format
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("group %d {\n", id));
        res.append("\ttests ").append(testsIntervals.stream().map(Interval::toString)
                .collect(Collectors.joining(","))).append(";\n");
        res.append("\t").append(String.format("score %d;\n", score));

        if (pointsPolicy == PointsPolicy.EACH_TEST) {
            res.append("\ttest_all;\n");
        }

        if (dependencies != null) {
            res.append("\trequires ").append(dependencies.stream().map(Objects::toString)
                    .collect(Collectors.joining(","))).append(";\n");
        }

        if (pointsPolicy == PointsPolicy.EACH_TEST) {
            res.append("\ttest_score ").append(tests.get(0).getPoints()).append(";\n");
        }

        res.append("}");
        return res.toString();
    }
}
