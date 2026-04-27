package com.deathtanium.simplethrusters.compat;

import net.neoforged.fml.ModList;

public final class CompatLoader {
    private CompatLoader() {}

    public static boolean createAdditionLoaded() {
        return ModList.get().isLoaded("createaddition");
    }

    public static boolean mekanismLoaded() {
        return ModList.get().isLoaded("mekanism");
    }
}
