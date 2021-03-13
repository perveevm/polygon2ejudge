package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes problem test
 */
public class Test {
    private final int id;
    private final int group;
    private final int points;
    private final boolean isSample;

    /**
     * Constructs test by given parameters
     * @param id test ID
     * @param group given test group ID
     * @param points score of given test
     * @param isSample if given test is sample
     */
    public Test(final int id, final int group, final int points, final boolean isSample) {
        this.id = id;
        this.group = group;
        this.points = points;
        this.isSample = isSample;
    }

    public int getId() {
        return id;
    }

    public int getGroup() {
        return group;
    }

    public int getPoints() {
        return points;
    }

    public boolean isSample() {
        return isSample;
    }
}
