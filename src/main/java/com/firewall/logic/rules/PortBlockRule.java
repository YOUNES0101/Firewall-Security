package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

/**
 * Rule that blocks packets on a specific port or port range.
 */
@Getter
public class PortBlockRule extends AbstractRule {

    /** The start of the port range to block (inclusive) */
    private final int portStart;

    /** The end of the port range to block (inclusive) */
    private final int portEnd;

    /** Whether to check source, destination, or both ports */
    private final PortMatchMode matchMode;

    public enum PortMatchMode {
        SOURCE, // Block if source port matches
        DESTINATION, // Block if destination port matches
        BOTH // Block if either port matches
    }

    /**
     * Creates a port block rule for a single port (destination only).
     *
     * @param name     rule name
     * @param priority evaluation priority
     * @param port     the port to block
     */
    public PortBlockRule(String name, int priority, int port) {
        this(name, priority, port, port, PortMatchMode.DESTINATION);
    }

    /**
     * Creates a port block rule for a range of ports.
     *
     * @param name      rule name
     * @param priority  evaluation priority
     * @param portStart start of port range (inclusive)
     * @param portEnd   end of port range (inclusive)
     * @param matchMode how to match ports
     */
    public PortBlockRule(String name, int priority, int portStart, int portEnd, PortMatchMode matchMode) {
        super(name, priority);
        if (portStart < 0 || portEnd > 65535 || portStart > portEnd) {
            throw new IllegalArgumentException("Invalid port range: " + portStart + "-" + portEnd);
        }
        this.portStart = portStart;
        this.portEnd = portEnd;
        this.matchMode = matchMode;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) {
            return false;
        }

        return switch (matchMode) {
            case SOURCE -> isInRange(packet.getSourcePort());
            case DESTINATION -> isInRange(packet.getDestPort());
            case BOTH -> isInRange(packet.getSourcePort()) || isInRange(packet.getDestPort());
        };
    }

    private boolean isInRange(Integer port) {
        if (port == null) {
            return false;
        }
        return port >= portStart && port <= portEnd;
    }

    @Override
    public String getDescription() {
        String portDesc = portStart == portEnd
                ? String.valueOf(portStart)
                : portStart + "-" + portEnd;
        return String.format("[PortBlockRule] %s - Block port(s): %s (%s)",
                name, portDesc, matchMode);
    }
}
