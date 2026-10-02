package net.officefloor.hq.app;

/** Request body for attaching a tag to a project: which tag (tagId) to add. */
public class NewProjectTag {

    private Long tagId;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
