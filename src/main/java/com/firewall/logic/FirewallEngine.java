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
     * Evaluates a packet against all active rules.
     *
     * @param packet the packet to evaluate
     * @return true if the packet should be BLOCKED, false if allowed
     */
    public boolean shouldBlock(Packet packet) {
        for (FilterRule rule : rules) {
            if (rule.matches(packet)) {
                return true; // First matching rule blocks the packet
            }
        }
        return false; // No rules matched, allow the packet
    }

    /**
     * Gets the first rule that matches the given packet.
     *
     * @param packet the packet to check
     * @return the matching rule, or null if no rule matches
     */
    public FilterRule getMatchingRule(Packet packet) {
        for (FilterRule rule : rules) {
            if (rule.matches(packet)) {
                return rule;
            }
        }
        return null;
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
