package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import lombok.Getter;

/**
 * Rule that blocks packets of a specific protocol type.
 */
@Getter
public class ProtocolBlockRule extends AbstractRule {

    private final ProtocolType blockedProtocol;

    public ProtocolBlockRule(String name, int priority, ProtocolType protocol) {
        super(name, priority);
        this.blockedProtocol = protocol;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null || packet.getProtocol() == null) {
            return false;
        }
        return blockedProtocol == packet.getProtocol();
    }

    @Override
    public String getDescription() {
        return String.format("[ProtocolBlockRule] %s - Block: %s", name, blockedProtocol);
    }
}
