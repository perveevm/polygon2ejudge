package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes integer interval
 */
public class Interval {
    public final int from;
    public final int to;

    /**
     * Constructs interval by two points
     *
     * @param from begin of the interval
     * @param to   end of the interval
     */
    public Interval(final int from, final int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return from + "-" + to;
    }
}
