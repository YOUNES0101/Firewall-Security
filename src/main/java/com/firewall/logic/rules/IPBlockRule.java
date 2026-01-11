package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

/**
 * Rule that blocks packets from or to a specific IP address.
 */
@Getter
public class IPBlockRule extends AbstractRule {

    /** The IP address to block */
    private final String blockedIP;

    /** Whether to block source, destination, or both */
    private final IPMatchMode matchMode;

    public enum IPMatchMode {
        SOURCE, // Block if source IP matches
        DESTINATION, // Block if destination IP matches
        BOTH // Block if either source or destination matches
    }

    /**
     * Creates an IP block rule that blocks both source and destination.
     *
     * @param name      rule name
     * @param priority  evaluation priority
     * @param blockedIP the IP address to block
     */
    public IPBlockRule(String name, int priority, String blockedIP) {
        this(name, priority, blockedIP, IPMatchMode.BOTH);
    }

    /**
     * Creates an IP block rule with a specific match mode.
     *
     * @param name      rule name
     * @param priority  evaluation priority
     * @param blockedIP the IP address to block
     * @param matchMode how to match the IP (source, destination, or both)
     */
    public IPBlockRule(String name, int priority, String blockedIP, IPMatchMode matchMode) {
        super(name, priority);
        this.blockedIP = blockedIP;
        this.matchMode = matchMode;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) {
            return false;
        }

        return switch (matchMode) {
            case SOURCE -> blockedIP.equals(packet.getSourceIP());
            case DESTINATION -> blockedIP.equals(packet.getDestIP());
            case BOTH -> blockedIP.equals(packet.getSourceIP()) ||
                    blockedIP.equals(packet.getDestIP());
        };
    }

    @Override
    public String getDescription() {
        return String.format("[IPBlockRule] %s - Block IP: %s (%s)",
                name, blockedIP, matchMode);
    }
}
