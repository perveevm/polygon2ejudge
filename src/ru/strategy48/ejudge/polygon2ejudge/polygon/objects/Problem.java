package ru.strategy48.ejudge.polygon2ejudge.polygon.objects;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Describe Polygon problem
 */
public class Problem {
    private final int id;
    private final String owner;
    private final String name;
    private final boolean deleted;
    private final boolean favourite;
    private final int revision;
    private final int latestPackage;
    private final boolean modified;

    public Problem(final int id, final String owner, final String name, final boolean deleted, final boolean favourite,
                   final int revision, final int latestPackage, final boolean modified) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.deleted = deleted;
        this.favourite = favourite;
        this.revision = revision;
        this.latestPackage = latestPackage;
        this.modified = modified;
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public int getRevision() {
        return revision;
    }

    public int getLatestPackage() {
        return latestPackage;
    }

    public boolean isModified() {
        return modified;
    }
}
