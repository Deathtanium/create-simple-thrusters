package com.deathtanium.simplethrusters.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class ThrustersPartials {
    /** Copied from Create Crafts & Additions (`guage.json` spelling). */
    public static final PartialModel FE_GAUGE_BG = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath("simple_thrusters", "block/modular_accumulator_gauge"));
    public static final PartialModel FE_GAUGE_DIAL = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath("simple_thrusters", "block/modular_accumulator_dial"));

    private ThrustersPartials() {}

    public static void init() {}
}
