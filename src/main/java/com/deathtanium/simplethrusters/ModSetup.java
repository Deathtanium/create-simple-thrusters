package com.deathtanium.simplethrusters;

import com.deathtanium.simplethrusters.content.thruster.ThrusterBlock;
import com.deathtanium.simplethrusters.content.thruster.ThrusterPlacementHelper;
import com.deathtanium.simplethrusters.registry.ModBlocks;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public final class ModSetup {
    private ModSetup() {}

    public static void registerPlacementHelpers() {
        Predicate<ItemStack> itemPredicate = stack ->
                stack.is(ModBlocks.CREATIVE_THRUSTER.asItem())
                        || stack.is(ModBlocks.ION_THRUSTER.asItem())
                        || stack.is(ModBlocks.BLAZER_THRUSTER.asItem());
        PlacementHelpers.register(new ThrusterPlacementHelper(itemPredicate, ModSetup::isThrusterState));
    }

    private static boolean isThrusterState(BlockState state) {
        return state.getBlock() instanceof ThrusterBlock;
    }
}
