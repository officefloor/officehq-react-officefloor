package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/contacts/{contactId}/primary — make this contact the main (primary) contact for its
 * client. A client has at most one main contact, so the flag is cleared on the client's other
 * contacts and set on this one. Appends one audit record
 * (CONTACT_PRIMARY_SET clientId=&lt;clientId&gt; contactId=&lt;contactId&gt;) so there is a record
 * it happened, and echoes back the saved contact. An unknown contact is rejected with 400 and
 * nothing is written.
 */
public class SetPrimaryContactLogic {

    public void service(@HttpPathParameter("contactId") String contactId, ContactRepository contacts,
            Audit audit, ObjectResponse<ContactView> response) {
        Long id = Long.valueOf(contactId);
        Contact contact = contacts.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing contact is required"));
        List<Contact> siblings = contacts.findByClientIdOrderByIdAsc(contact.getClientId());
        Contact chosen = contact;
        for (Contact c : siblings) {
            boolean shouldBePrimary = c.getId().equals(id);
            if (c.isPrimary() != shouldBePrimary) {
                c.setPrimary(shouldBePrimary);
                Contact saved = contacts.save(c);
                if (shouldBePrimary) {
                    chosen = saved;
                }
            } else if (shouldBePrimary) {
                chosen = c;
            }
        }
        audit.record("CONTACT_PRIMARY_SET clientId=" + chosen.getClientId() + " contactId=" + id);
        response.send(new ContactView(chosen.getId(), chosen.getClientId(), chosen.getName(),
                chosen.getEmail(), chosen.getRole(), chosen.isPrimary()));
    }
}
