package com.deathtanium.simplethrusters.registry;

import com.deathtanium.simplethrusters.SimpleThrusters;
import com.deathtanium.simplethrusters.compat.CompatLoader;
import com.deathtanium.simplethrusters.compat.MekanismFluidBridge;
import com.deathtanium.simplethrusters.fluids.SimpleGasFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, SimpleThrusters.MOD_ID);

    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID.key(), SimpleThrusters.MOD_ID);

    private static SimpleGasFluid createHydrogenStill() {
        return new SimpleGasFluid(
                new BaseFlowingFluid.Properties(HYDROGEN_TYPE, HYDROGEN_STILL::get, HYDROGEN_FLOWING::get), true);
    }

    private static SimpleGasFluid createHydrogenFlowing() {
        return new SimpleGasFluid(
                new BaseFlowingFluid.Properties(HYDROGEN_TYPE, HYDROGEN_STILL::get, HYDROGEN_FLOWING::get), false);
    }

    private static SimpleGasFluid createOxygenStill() {
        return new SimpleGasFluid(
                new BaseFlowingFluid.Properties(OXYGEN_TYPE, OXYGEN_STILL::get, OXYGEN_FLOWING::get), true);
    }

    private static SimpleGasFluid createOxygenFlowing() {
        return new SimpleGasFluid(
                new BaseFlowingFluid.Properties(OXYGEN_TYPE, OXYGEN_STILL::get, OXYGEN_FLOWING::get), false);
    }

    public static final DeferredHolder<FluidType, FluidType> HYDROGEN_TYPE = FLUID_TYPES.register(
            "hydrogen",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .density(-10)
                            .temperature(300)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<FluidType, FluidType> OXYGEN_TYPE = FLUID_TYPES.register(
            "oxygen",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .density(-10)
                            .temperature(300)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, Fluid> HYDROGEN_STILL =
            FLUIDS.register("hydrogen_still", ModFluids::createHydrogenStill);

    public static final DeferredHolder<Fluid, Fluid> HYDROGEN_FLOWING =
            FLUIDS.register("hydrogen_flowing", ModFluids::createHydrogenFlowing);

    public static final DeferredHolder<Fluid, Fluid> OXYGEN_STILL =
            FLUIDS.register("oxygen_still", ModFluids::createOxygenStill);

    public static final DeferredHolder<Fluid, Fluid> OXYGEN_FLOWING =
            FLUIDS.register("oxygen_flowing", ModFluids::createOxygenFlowing);

    private ModFluids() {}

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    public static Fluid hydrogenStill() {
        if (CompatLoader.mekanismLoaded()) {
            Fluid m = MekanismFluidBridge.hydrogenOrNull();
            if (m != null && m != Fluids.EMPTY) return m;
        }
        return HYDROGEN_STILL.get();
    }

    public static Fluid oxygenStill() {
        if (CompatLoader.mekanismLoaded()) {
            Fluid m = MekanismFluidBridge.oxygenOrNull();
            if (m != null && m != Fluids.EMPTY) return m;
        }
        return OXYGEN_STILL.get();
    }
}
