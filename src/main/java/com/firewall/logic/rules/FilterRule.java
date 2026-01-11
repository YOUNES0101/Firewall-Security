package com.firewall.logic.rules;

import com.firewall.domain.Packet;

/**
 * Interface for firewall filtering rules.
 * Each rule determines if a packet should be blocked or allowed.
 */
public interface FilterRule {

    /**
     * Checks if the given packet matches this blocking rule.
     *
     * @param packet the packet to evaluate
     * @return true if the packet should be BLOCKED, false to allow
     */
    boolean matches(Packet packet);

    /**
     * Returns a human-readable description of this rule.
     *
     * @return rule description
     */
    String getDescription();
}
