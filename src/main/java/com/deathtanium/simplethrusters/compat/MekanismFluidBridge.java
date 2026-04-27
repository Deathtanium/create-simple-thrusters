package com.deathtanium.simplethrusters.compat;

import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Resolves Mekanism registered hydrogen/oxygen fluids at runtime when Mekanism is present.
 * Avoids a compile-time dependency on Mekanism while matching pipe/spill behavior for the same fluids.
 */
public final class MekanismFluidBridge {
    @Nullable
    private static Fluid cachedHydrogen;

    @Nullable
    private static Fluid cachedOxygen;

    private MekanismFluidBridge() {}

    public static void init() {
        if (!CompatLoader.mekanismLoaded()) return;
        try {
            Class<?> reg = Class.forName("mekanism.common.registries.MekanismFluids");
            cachedHydrogen = extractStillFluid(reg.getField("HYDROGEN"));
            cachedOxygen = extractStillFluid(reg.getField("OXYGEN"));
        } catch (Throwable ignored) {
            cachedHydrogen = null;
            cachedOxygen = null;
        }
    }

    @Nullable
    private static Fluid extractStillFluid(Field field) throws Exception {
        Object holder = field.get(null);
        Method get = holder.getClass().getMethod("get");
        Object fluid = get.invoke(holder);
        return fluid instanceof Fluid f ? f : null;
    }

    @Nullable
    public static Fluid hydrogenOrNull() {
        return cachedHydrogen;
    }

    @Nullable
    public static Fluid oxygenOrNull() {
        return cachedOxygen;
    }
}
