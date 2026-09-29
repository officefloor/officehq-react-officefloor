package net.officefloor.hq.app;

/** Request body for choosing a client's main contact (POST /api/contacts/primary). */
public class SetPrimaryContact {

    private long clientId;
    private long contactId;

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }
}
