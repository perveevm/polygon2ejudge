package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Class that represents test
 */
public class Test {
    private final int id;
    private final GenerationMethod method;
    private final String cmd;
    private final String group;
    private final int points;
    private final boolean sample;
    private final String fromFile;

    public Test(final int id, final GenerationMethod method, final String cmd, final String group, final int points,
                final boolean sample, final String fromFile) {
        this.id = id;
        this.method = method;
        this.cmd = cmd;
        this.group = group;
        this.points = points;
        this.sample = sample;
        this.fromFile = fromFile;
    }

    public int getId() {
        return id;
    }

    public GenerationMethod getMethod() {
        return method;
    }

    public String getCmd() {
        return cmd;
    }

    public String getGroup() {
        return group;
    }

    public int getPoints() {
        return points;
    }

    public boolean isSample() {
        return sample;
    }

    public String getFromFile() {
        return fromFile;
    }
}
