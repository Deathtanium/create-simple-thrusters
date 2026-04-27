package com.deathtanium.simplethrusters.registry;

import com.deathtanium.simplethrusters.SimpleThrusters;
import com.deathtanium.simplethrusters.content.thruster.ThrusterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SimpleThrusters.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThrusterBlockEntity>> THRUSTER =
            REGISTER.register(
                    "thruster",
                    () -> BlockEntityType.Builder.of(
                                    ThrusterBlockEntity::new,
                                    ModBlocks.CREATIVE_THRUSTER.get(),
                                    ModBlocks.ION_THRUSTER.get(),
                                    ModBlocks.BLAZER_THRUSTER.get())
                            .build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
