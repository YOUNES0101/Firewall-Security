package com.firewall.capture;

import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Packet capture utility using Pcap4J.
 * Requires Npcap installed and administrator privileges on Windows.
 */
public class PacketCapture {

    private static final Logger log = LoggerFactory.getLogger(PacketCapture.class);
    private static final int SNAPSHOT_LENGTH = 65536;
    private static final int READ_TIMEOUT = 50; // ms

    private PcapHandle handle;
    private volatile boolean capturing = false;

    /**
     * Lists all available network interfaces.
     *
     * @return list of network interface descriptions
     */
    public static List<String> listInterfaces() {
        List<String> interfaces = new ArrayList<>();
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            for (int i = 0; i < allDevs.size(); i++) {
                PcapNetworkInterface nif = allDevs.get(i);
                String desc = nif.getDescription() != null ? nif.getDescription() : nif.getName();
                interfaces.add(String.format("[%d] %s", i, desc));
            }
        } catch (PcapNativeException e) {
            log.error("Failed to list interfaces: {}", e.getMessage());
            interfaces.add("Error: " + e.getMessage());
        }
        return interfaces;
    }

    /**
     * Gets all available network interfaces.
     *
     * @return list of PcapNetworkInterface objects
     * @throws PcapNativeException if Npcap is not available
     */
    public static List<PcapNetworkInterface> getNetworkInterfaces() throws PcapNativeException {
        return Pcaps.findAllDevs();
    }

    /**
     * Starts capturing packets on the specified interface.
     *
     * @param interfaceIndex the index of the interface to capture on
     * @param packetHandler  callback for each captured packet
     * @param maxPackets     maximum number of packets to capture (0 = unlimited)
     */
    public void startCapture(int interfaceIndex, Consumer<Packet> packetHandler, int maxPackets) {
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (interfaceIndex < 0 || interfaceIndex >= allDevs.size()) {
                log.error("Invalid interface index: {}", interfaceIndex);
                return;
            }

            PcapNetworkInterface nif = allDevs.get(interfaceIndex);
            handle = nif.openLive(SNAPSHOT_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

            capturing = true;
            int count = 0;

            log.info("Started capturing on: {}", nif.getDescription());

            while (capturing && (maxPackets == 0 || count < maxPackets)) {
                org.pcap4j.packet.Packet rawPacket = handle.getNextPacket();
                if (rawPacket != null) {
                    Packet packet = convertPacket(rawPacket);
                    if (packet != null) {
                        packetHandler.accept(packet);
                        count++;
                    }
                }
            }

        } catch (PcapNativeException | NotOpenException e) {
            log.error("Capture error: {}", e.getMessage());
        } finally {
            stopCapture();
        }
    }

    /**
     * Stops the current packet capture.
     */
    public void stopCapture() {
        capturing = false;
        if (handle != null && handle.isOpen()) {
            handle.close();
            log.info("Capture stopped");
        }
    }

    /**
     * Converts a raw Pcap4J packet to our domain Packet DTO.
     */
    private Packet convertPacket(org.pcap4j.packet.Packet rawPacket) {
        IpV4Packet ipPacket = rawPacket.get(IpV4Packet.class);
        if (ipPacket == null) {
            return null; // Skip non-IPv4 packets
        }

        Packet.PacketBuilder builder = Packet.builder()
                .sourceIP(ipPacket.getHeader().getSrcAddr().getHostAddress())
                .destIP(ipPacket.getHeader().getDstAddr().getHostAddress())
                .timestamp(java.time.Instant.now())
                .payloadSize(rawPacket.length());

        // Check for TCP
        TcpPacket tcpPacket = rawPacket.get(TcpPacket.class);
        if (tcpPacket != null) {
            builder.protocol(ProtocolType.TCP)
                    .sourcePort(tcpPacket.getHeader().getSrcPort().valueAsInt())
                    .destPort(tcpPacket.getHeader().getDstPort().valueAsInt());
            return builder.build();
        }

        // Check for UDP
        UdpPacket udpPacket = rawPacket.get(UdpPacket.class);
        if (udpPacket != null) {
            builder.protocol(ProtocolType.UDP)
                    .sourcePort(udpPacket.getHeader().getSrcPort().valueAsInt())
                    .destPort(udpPacket.getHeader().getDstPort().valueAsInt());
            return builder.build();
        }

        // Other protocol (ICMP, etc.)
        builder.protocol(ProtocolType.UNKNOWN);
        return builder.build();
    }
}
