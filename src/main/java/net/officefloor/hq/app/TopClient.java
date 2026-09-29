package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * One row of the home dashboard's "top clients" panel: a client and how much they still owe, so the
 * panel can rank clients by amount owed. Serialised as JSON by GET /api/dashboard/top-clients.
 */
public record TopClient(long clientId, String name, BigDecimal outstanding) {
}
