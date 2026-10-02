package net.officefloor.hq.app.tags;

import net.officefloor.web.HttpObject;

/** JSON request body for putting a tag on a project. {@link HttpObject} loads it from the entity. */
@HttpObject
public class NewProjectTag {

    private Long tagId;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
