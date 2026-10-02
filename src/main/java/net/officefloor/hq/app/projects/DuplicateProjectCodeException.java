package net.officefloor.hq.app.projects;

/** Raised when creating a project whose reference code is already used by another project. */
public class DuplicateProjectCodeException extends RuntimeException {

    public DuplicateProjectCodeException(String code) {
        super("A project already uses code " + code);
    }
}
