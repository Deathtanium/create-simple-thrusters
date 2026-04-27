package com.deathtanium.simplethrusters.content.electrolysis;

import com.deathtanium.simplethrusters.SimpleThrusters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SimpleThrusters.MOD_ID)
public final class ElectrolysisEvents {
    private ElectrolysisEvents() {}

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.COPPER_GRATE)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (ElectrolysisChamberBlockEntity.tryConvertColumn(level, pos)) {
            if (!event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }
}
