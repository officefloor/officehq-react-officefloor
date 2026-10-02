package net.officefloor.hq.app.clients;

/** Raised when creating a client whose email is already used by another client. */
public class DuplicateClientEmailException extends RuntimeException {

    public DuplicateClientEmailException(String email) {
        super("A client already uses email " + email);
    }
}
