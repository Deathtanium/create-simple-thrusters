package com.deathtanium.simplethrusters.content.thruster;

import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ThrusterCapabilitySetup {
    private ThrusterCapabilitySetup() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.THRUSTER.get(),
                (be, ctx) -> be instanceof ThrusterBlockEntity thruster ? thruster.fluidHandlerForSide(ctx) : null);
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.THRUSTER.get(),
                (be, ctx) -> be instanceof ThrusterBlockEntity thruster ? thruster.energyStorageForSide(ctx) : null);
    }
}
