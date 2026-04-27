package com.deathtanium.simplethrusters;

import com.deathtanium.simplethrusters.registry.ModBlocks;
import com.deathtanium.simplethrusters.registry.ModCreativeTabs;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SimpleThrusters.MOD_ID)
public final class SimpleThrusters {
    public static final String MOD_ID = "simple_thrusters";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleThrusters(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        modEventBus.addListener(ModEvents::registerCapabilities);
    }
}
