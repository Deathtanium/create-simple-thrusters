package com.deathtanium.simplethrusters.content.thruster;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FuelCompatibility {
    private FuelCompatibility() {}

    public static boolean isIonFuel(FluidStack stack) {
        return matchesTokens(stack, "liquid_hydrogen", "hydrogen", "liquid_xenon", "xenon");
    }

    /**
     * Blazer accepts broader oxidizer/fuel including oxygen and kerosene-like fluids.
     */
    public static boolean isBlazerFuel(FluidStack stack) {
        return isIonFuel(stack)
                || matchesTokens(stack, "oxygen", "liquid_oxygen", "kerosene", "fuel", "diesel", "gasoline");
    }

    private static boolean matchesTokens(FluidStack stack, String... tokens) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        String path = id.getPath().toLowerCase(Locale.ROOT);
        String ns = id.getNamespace().toLowerCase(Locale.ROOT);
        for (String t : tokens) {
            if (path.contains(t) || ns.contains(t)) return true;
        }
        return false;
    }
}
