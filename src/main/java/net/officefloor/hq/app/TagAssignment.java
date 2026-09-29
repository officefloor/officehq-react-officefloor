package net.officefloor.hq.app;

/** Request body for adding / removing a tag on a project (POST /api/projects/tags[/remove]). */
public class TagAssignment {

    private long projectId;
    private long tagId;

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
