package com.deathtanium.simplethrusters;

import com.deathtanium.simplethrusters.content.thruster.ThrusterCapabilitySetup;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModEvents {
    private ModEvents() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ThrusterCapabilitySetup.register(event);
    }
}
