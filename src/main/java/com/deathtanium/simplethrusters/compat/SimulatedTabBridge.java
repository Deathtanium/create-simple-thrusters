package com.deathtanium.simplethrusters.compat;

import com.deathtanium.simplethrusters.registry.ModBlocks;
import com.deathtanium.simplethrusters.SimpleThrusters;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Registers our block items into Create Simulated's Aeronautics creative subsection (banner row).
 * Mirrors {@code dev.eriksonn.aeronautics.Aeronautics#getRegistrate()} section
 * {@code Aeronautics.path("aeronautics")} without a compile-time dependency on simulated/aeronautics jars.
 */
public final class SimulatedTabBridge {
    /** Same resource location as Aeronautics uses for its registrate section. */
    public static final ResourceLocation AERONAUTICS_CREATIVE_SECTION =
            ResourceLocation.fromNamespaceAndPath("aeronautics", "aeronautics");

    private SimulatedTabBridge() {}

    /**
     * Must run on common setup after registry events (items exist in {@link net.minecraft.core.registries.BuiltInRegistries}).
     */
    public static void registerThrustItemsIntoAeronauticsSection(Logger log) {
        try {
            Class<?> sr =
                    Class.forName("dev.simulated_team.simulated.registrate.SimulatedRegistrate");
            Constructor<?> ctor =
                    sr.getConstructor(ResourceLocation.class, String.class);
            Method addExtra =
                    sr.getMethod("addExtraItem", ResourceLocation.class);

            Object registrate =
                    ctor.newInstance(AERONAUTICS_CREATIVE_SECTION, SimpleThrusters.MOD_ID);

            add(registrate, addExtra, ModBlocks.CREATIVE_THRUSTER.getId());
            add(registrate, addExtra, ModBlocks.ION_THRUSTER.getId());
            add(registrate, addExtra, ModBlocks.BLAZER_THRUSTER.getId());
            add(registrate, addExtra, ModBlocks.ELECTROLYSIS_CHAMBER.getId());
        } catch (Throwable t) {
            log.warn(
                    "Could not attach Simple Thrusters items to Aeronautics creative subsection (simulated integration): {}",
                    t.toString());
        }
    }

    private static void add(Object registrate, Method addExtra, ResourceLocation itemId)
            throws Exception {
        addExtra.invoke(registrate, itemId);
    }
}
