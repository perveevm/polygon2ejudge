package ru.strategy48.ejudge.polygon2ejudge.polygon.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describes Polygon problem package
 */
public class Package {
    private final int id;
    private final int revision;
    private final int creationTimeSeconds;
    private final PackageState state;
    private final String comment;

    public Package(final int id, final int revision, final int creationTimeSeconds, final PackageState state, final String comment) {
        this.id = id;
        this.revision = revision;
        this.creationTimeSeconds = creationTimeSeconds;
        this.state = state;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public int getRevision() {
        return revision;
    }

    public int getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public PackageState getState() {
        return state;
    }

    public String getComment() {
        return comment;
    }
}
