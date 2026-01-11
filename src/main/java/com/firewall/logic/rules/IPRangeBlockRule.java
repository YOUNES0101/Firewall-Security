package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

/**
 * Rule that blocks packets from a specific IP range (CIDR notation).
 * Example: 192.168.1.0/24 blocks all IPs from 192.168.1.0 to 192.168.1.255
 */
@Getter
public class IPRangeBlockRule extends AbstractRule {

    private final String networkAddress;
    private final int prefixLength;
    private final long networkLong;
    private final long maskLong;

    /**
     * Creates an IP range block rule.
     *
     * @param name     rule name
     * @param priority evaluation priority
     * @param cidr     CIDR notation (e.g., "192.168.1.0/24")
     */
    public IPRangeBlockRule(String name, int priority, String cidr) {
        super(name, priority);

        String[] parts = cidr.split("/");
        this.networkAddress = parts[0];
        this.prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;

        this.networkLong = ipToLong(networkAddress);
        this.maskLong = prefixLength == 0 ? 0 : (0xFFFFFFFFL << (32 - prefixLength));
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) {
            return false;
        }

        // Check source IP
        if (packet.getSourceIP() != null && isInRange(packet.getSourceIP())) {
            return true;
        }

        // Check destination IP
        if (packet.getDestIP() != null && isInRange(packet.getDestIP())) {
            return true;
        }

        return false;
    }

    private boolean isInRange(String ip) {
        try {
            long ipLong = ipToLong(ip);
            return (ipLong & maskLong) == (networkLong & maskLong);
        } catch (Exception e) {
            return false;
        }
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (Integer.parseInt(octets[i]) & 0xFF);
        }
        return result;
    }

    @Override
    public String getDescription() {
        return String.format("[IPRangeBlockRule] %s - Block: %s/%d", name, networkAddress, prefixLength);
    }
}
