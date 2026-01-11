package com.firewall;

import com.firewall.capture.PacketCapture;
import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import com.firewall.logic.FirewallEngine;
import com.firewall.logic.rules.IPBlockRule;
import com.firewall.logic.rules.PortBlockRule;

/**
 * CLI entry point for quick smoke tests.
 * Run this to verify the firewall logic with generated packets.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("=== Firewall Security - CLI Mode ===\n");

        // 1. List network interfaces
        System.out.println("Available Network Interfaces:");
        PacketCapture.listInterfaces().forEach(System.out::println);
        System.out.println();

        // 2. Demo firewall engine with test packets
        System.out.println("Testing Firewall Engine:");
        FirewallEngine engine = new FirewallEngine();

        // Add some rules
        engine.addRule(new IPBlockRule("Block Malicious IP", 100, "192.168.1.100"));
        engine.addRule(new PortBlockRule("Block SSH", 90, 22));
        engine.addRule(new PortBlockRule("Block Telnet", 90, 23));

        System.out.println("Active Rules:");
        engine.getRules().forEach(r -> System.out.println("  - " + r.getDescription()));
        System.out.println();

        // Test packets
        Packet[] testPackets = {
                Packet.of("192.168.1.100", "10.0.0.1", 12345, 80, ProtocolType.TCP),
                Packet.of("10.0.0.5", "10.0.0.1", 54321, 22, ProtocolType.TCP),
                Packet.of("10.0.0.5", "10.0.0.1", 54321, 443, ProtocolType.TCP),
                Packet.of("8.8.8.8", "10.0.0.1", 53, 53, ProtocolType.UDP),
        };

        System.out.println("Packet Evaluation Results:");
        for (Packet packet : testPackets) {
            boolean blocked = engine.shouldBlock(packet);
            String status = blocked ? "BLOCKED" : "ALLOWED";
            System.out.printf("  %s:%d -> %s:%d [%s] => %s%n",
                    packet.getSourceIP(), packet.getSourcePort(),
                    packet.getDestIP(), packet.getDestPort(),
                    packet.getProtocol(), status);
        }

        System.out.println("\n=== CLI Test Complete ===");
    }
}
