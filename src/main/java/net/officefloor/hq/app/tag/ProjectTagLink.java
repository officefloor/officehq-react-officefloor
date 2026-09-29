package net.officefloor.hq.app.tag;

/** One project-to-tag attachment as the UI surfaces it: which project carries which tag. */
public class ProjectTagLink {

    private final Long projectId;
    private final Long tagId;

    public ProjectTagLink(Long projectId, Long tagId) {
        this.projectId = projectId;
        this.tagId = tagId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getTagId() {
        return tagId;
    }
}
