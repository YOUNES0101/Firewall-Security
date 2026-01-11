package com.firewall.logic;

import com.firewall.domain.Packet;
import com.firewall.logic.rules.AbstractRule;
import com.firewall.logic.rules.FilterRule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Core firewall engine that evaluates packets against a set of rules.
 */
@Getter
public class FirewallEngine {

    private final List<FilterRule> rules = new ArrayList<>();

    /**
     * Adds a rule to the firewall.
     *
     * @param rule the rule to add
     */
    public void addRule(FilterRule rule) {
        rules.add(rule);
        sortRulesByPriority();
    }

    /**
     * Removes a rule from the firewall.
     *
     * @param rule the rule to remove
     */
    public void removeRule(FilterRule rule) {
        rules.remove(rule);
    }

    /**
     * Clears all rules from the firewall.
     */
    public void clearRules() {
        rules.clear();
    }

    /**
     * Evaluates a packet against all active rules using Java Streams.
     * Demonstrates: Stream API, Lambda Expressions, Method References, anyMatch()
     *
     * @param packet the packet to evaluate
     * @return true if the packet should be BLOCKED, false if allowed
     */
    public boolean shouldBlock(Packet packet) {
        // Using Stream API with Lambda for functional-style filtering
        return rules.stream()
                .filter(rule -> rule instanceof AbstractRule ar && ar.isEnabled())
                .anyMatch(rule -> rule.matches(packet));
    }

    /**
     * Gets the first rule that matches the given packet using Optional.
     * Demonstrates: Stream API, filter(), findFirst(), Optional
     *
     * @param packet the packet to check
     * @return Optional containing the matching rule, or empty if none matches
     */
    public java.util.Optional<FilterRule> findMatchingRule(Packet packet) {
        return rules.stream()
                .filter(rule -> rule instanceof AbstractRule ar && ar.isEnabled())
                .filter(rule -> rule.matches(packet))
                .findFirst();
    }

    /**
     * Gets the first rule that matches the given packet.
     * Kept for backward compatibility.
     *
     * @param packet the packet to check
     * @return the matching rule, or null if no rule matches
     */
    public FilterRule getMatchingRule(Packet packet) {
        return findMatchingRule(packet).orElse(null);
    }

    /**
     * Gets all rules matching a packet using Streams.
     * Demonstrates: Stream API, filter(), collect(), Collectors.toList()
     *
     * @param packet the packet to check
     * @return list of all matching rules
     */
    public List<FilterRule> getAllMatchingRules(Packet packet) {
        return rules.stream()
                .filter(rule -> rule.matches(packet))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Counts active rules using Streams.
     * Demonstrates: Stream API, filter(), count()
     *
     * @return number of enabled rules
     */
    public long countActiveRules() {
        return rules.stream()
                .filter(rule -> rule instanceof AbstractRule ar && ar.isEnabled())
                .count();
    }

    private void sortRulesByPriority() {
        rules.sort((r1, r2) -> {
            if (r1 instanceof AbstractRule ar1 && r2 instanceof AbstractRule ar2) {
                return Integer.compare(ar2.getPriority(), ar1.getPriority()); // Higher first
            }
            return 0;
        });
    }
}
