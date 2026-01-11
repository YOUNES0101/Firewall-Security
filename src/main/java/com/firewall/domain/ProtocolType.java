package com.firewall.domain;

/**
 * Supported network protocol types for packet filtering.
 */
public enum ProtocolType {
    TCP,
    UDP,
    ICMP,
    UNKNOWN;

    /**
     * Parses a protocol name to its corresponding enum value.
     *
     * @param name the protocol name (case-insensitive)
     * @return the matching ProtocolType, or UNKNOWN if not recognized
     */
    public static ProtocolType fromString(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        return switch (name.toUpperCase()) {
            case "TCP" -> TCP;
            case "UDP" -> UDP;
            case "ICMP" -> ICMP;
            default -> UNKNOWN;
        };
    }
}
