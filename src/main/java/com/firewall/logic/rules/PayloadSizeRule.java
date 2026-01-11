package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

/**
 * Rule that blocks packets by payload size threshold.
 * Useful for blocking large packets or potential attacks.
 */
@Getter
public class PayloadSizeRule extends AbstractRule {

    private final int minSize;
    private final int maxSize;

    /**
     * Creates a payload size rule.
     *
     * @param name     rule name
     * @param priority evaluation priority
     * @param minSize  minimum size to match (0 for no minimum)
     * @param maxSize  maximum size to match (Integer.MAX_VALUE for no maximum)
     */
    public PayloadSizeRule(String name, int priority, int minSize, int maxSize) {
        super(name, priority);
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    /**
     * Creates a rule that blocks packets larger than the specified size.
     */
    public static PayloadSizeRule blockLargerThan(String name, int priority, int size) {
        return new PayloadSizeRule(name, priority, size + 1, Integer.MAX_VALUE);
    }

    /**
     * Creates a rule that blocks packets smaller than the specified size.
     */
    public static PayloadSizeRule blockSmallerThan(String name, int priority, int size) {
        return new PayloadSizeRule(name, priority, 0, size - 1);
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) {
            return false;
        }
        int size = packet.getPayloadSize();
        return size >= minSize && size <= maxSize;
    }

    @Override
    public String getDescription() {
        if (minSize == 0) {
            return String.format("[PayloadSizeRule] %s - Block packets < %d bytes", name, maxSize + 1);
        } else if (maxSize == Integer.MAX_VALUE) {
            return String.format("[PayloadSizeRule] %s - Block packets > %d bytes", name, minSize - 1);
        }
        return String.format("[PayloadSizeRule] %s - Block packets %d-%d bytes", name, minSize, maxSize);
    }
}
