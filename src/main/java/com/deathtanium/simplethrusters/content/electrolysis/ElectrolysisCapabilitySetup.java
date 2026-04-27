package com.deathtanium.simplethrusters.content.electrolysis;

import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ElectrolysisCapabilitySetup {
    private ElectrolysisCapabilitySetup() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.ELECTROLYSIS_CHAMBER.get(),
                (be, ctx) -> be instanceof ElectrolysisChamberBlockEntity e ? e.fluidHandler(ctx) : null);
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ELECTROLYSIS_CHAMBER.get(),
                (be, ctx) -> be instanceof ElectrolysisChamberBlockEntity e ? e.energyHandler(ctx) : null);
    }
}
