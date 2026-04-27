package com.deathtanium.simplethrusters.registry;

import com.deathtanium.simplethrusters.SimpleThrusters;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SimpleThrusters.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.simple_thrusters.main"))
                    .icon(() -> new ItemStack(ModBlocks.CREATIVE_THRUSTER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.CREATIVE_THRUSTER.asStack());
                        output.accept(ModBlocks.ION_THRUSTER.asStack());
                        output.accept(ModBlocks.BLAZER_THRUSTER.asStack());
                        output.accept(ModBlocks.ELECTROLYSIS_CHAMBER.asStack());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
