package net.officefloor.hq.app.projects;

/**
 * JSON response shape for creating a project. Either the row was created ({@code project} set), or
 * it was rejected because another project already uses that reference code ({@code codeInUse} true)
 * — the UI branches on this to surface the duplicate-code error without adding a row.
 */
public class CreateProjectResult {

    private final ProjectView project;
    private final boolean codeInUse;

    private CreateProjectResult(ProjectView project, boolean codeInUse) {
        this.project = project;
        this.codeInUse = codeInUse;
    }

    public static CreateProjectResult created(ProjectView project) {
        return new CreateProjectResult(project, false);
    }

    public static CreateProjectResult codeInUse() {
        return new CreateProjectResult(null, true);
    }

    public ProjectView getProject() {
        return project;
    }

    public boolean isCodeInUse() {
        return codeInUse;
    }
}
