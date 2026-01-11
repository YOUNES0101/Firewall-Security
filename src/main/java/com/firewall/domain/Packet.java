package com.firewall.domain;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * Data Transfer Object representing a captured network packet.
 * Contains essential header information for firewall rule matching.
 */
@Data
@Builder
public class Packet {

    /** Source IP address (e.g., "192.168.1.100") */
    private String sourceIP;

    /** Destination IP address (e.g., "10.0.0.1") */
    private String destIP;

    /** Source port number (0-65535), may be null for ICMP */
    private Integer sourcePort;

    /** Destination port number (0-65535), may be null for ICMP */
    private Integer destPort;

    /** Network protocol type */
    private ProtocolType protocol;

    /** Timestamp when the packet was captured */
    private Instant timestamp;

    /** Raw packet payload size in bytes */
    private int payloadSize;

    /**
     * Creates a simple packet for testing purposes.
     *
     * @param sourceIP   source IP address
     * @param destIP     destination IP address
     * @param sourcePort source port
     * @param destPort   destination port
     * @param protocol   protocol type
     * @return a new Packet instance
     */
    public static Packet of(String sourceIP, String destIP,
            Integer sourcePort, Integer destPort,
            ProtocolType protocol) {
        return Packet.builder()
                .sourceIP(sourceIP)
                .destIP(destIP)
                .sourcePort(sourcePort)
                .destPort(destPort)
                .protocol(protocol)
                .timestamp(Instant.now())
                .payloadSize(0)
                .build();
    }
}
