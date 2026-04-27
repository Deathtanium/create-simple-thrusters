package com.deathtanium.simplethrusters;

import com.deathtanium.simplethrusters.compat.MekanismFluidBridge;
import com.deathtanium.simplethrusters.compat.SimulatedTabBridge;
import com.deathtanium.simplethrusters.registry.ModBlocks;
import com.deathtanium.simplethrusters.registry.ModFluids;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(SimpleThrusters.MOD_ID)
public final class SimpleThrusters {
    public static final String MOD_ID = "simple_thrusters";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleThrusters(IEventBus modEventBus, ModContainer modContainer) {
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        modEventBus.addListener(ModEvents::registerCapabilities);
        modEventBus.addListener(SimpleThrusters::commonSetup);
        ModSetup.registerPlacementHelpers();
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MekanismFluidBridge.init();
            SimulatedTabBridge.registerThrustItemsIntoAeronauticsSection(LOGGER);
        });
    }
}
