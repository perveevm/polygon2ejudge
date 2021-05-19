package ru.strategy48.ejudge.polygon2ejudge.contest.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Class that represents problem solution
 */
public class Solution {
    private final String tag;
    private final ProblemFile file;

    public Solution(final String tag, final ProblemFile file) {
        this.tag = tag;
        this.file = file;
    }

    public String getTag() {
        return tag;
    }

    public ProblemFile getFile() {
        return file;
    }
}
