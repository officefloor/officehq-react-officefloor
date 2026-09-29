package net.officefloor.hq.app.contact;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Contact}. */
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
