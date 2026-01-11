package com.firewall.logic.rules;

import lombok.Getter;

/**
 * Abstract base class for firewall rules.
 * Provides common functionality like rule naming and priority.
 */
@Getter
public abstract class AbstractRule implements FilterRule {

    /** Rule name for identification */
    protected final String name;

    /** Rule priority (higher = evaluated first) */
    protected final int priority;

    /** Whether this rule is currently active */
    protected boolean enabled;

    /**
     * Creates a new rule with the specified name and priority.
     *
     * @param name     unique rule identifier
     * @param priority evaluation priority (higher first)
     */
    protected AbstractRule(String name, int priority) {
        this.name = name;
        this.priority = priority;
        this.enabled = true;
    }

    /**
     * Enable or disable this rule.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getDescription() {
        return String.format("[%s] %s (priority: %d, enabled: %s)",
                getClass().getSimpleName(), name, priority, enabled);
    }
}
