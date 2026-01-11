package com.firewall.persistence.entity;

import com.firewall.domain.ProtocolType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * JPA Entity for storing captured packets in the database.
 */
@Entity
@Table(name = "captured_packets", indexes = {
        @Index(name = "idx_source_ip", columnList = "sourceIp"),
        @Index(name = "idx_dest_ip", columnList = "destIp"),
        @Index(name = "idx_timestamp", columnList = "capturedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedPacket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String sourceIp;

    @Column(nullable = false, length = 45)
    private String destIp;

    private Integer sourcePort;

    private Integer destPort;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ProtocolType protocol;

    private int payloadSize;

    @Column(nullable = false)
    private Instant capturedAt;

    /** The rule that blocked this packet (null if just logged) */
    @Column(length = 100)
    private String matchedRule;

    /** Session/scan identifier to group packets */
    @Column(length = 50)
    private String sessionId;

    /** Interface name where packet was captured */
    @Column(length = 100)
    private String interfaceName;
}
