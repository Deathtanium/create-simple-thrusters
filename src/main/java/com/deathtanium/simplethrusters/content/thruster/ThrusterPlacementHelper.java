package com.deathtanium.simplethrusters.content.thruster;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

/**
 * Sail-style extension: place another thruster of the same type along the nozzle axis (excluding that axis),
 * matching facing. Based on Create {@code SailBlock.PlacementHelper}.
 */
public final class ThrusterPlacementHelper implements IPlacementHelper {

    private final Predicate<ItemStack> itemPredicate;
    private final Predicate<BlockState> statePredicate;

    public ThrusterPlacementHelper(Predicate<ItemStack> itemPredicate, Predicate<BlockState> statePredicate) {
        this.itemPredicate = itemPredicate;
        this.statePredicate = statePredicate;
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return itemPredicate;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return statePredicate;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        Direction facing = state.getValue(ThrusterBlock.FACING);
        List<Direction> directions =
                IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), facing.getAxis(), dir -> world.getBlockState(pos.relative(dir)).canBeReplaced());

        if (directions.isEmpty()) return PlacementOffset.fail();

        return PlacementOffset.success(pos.relative(directions.get(0)), s -> s.setValue(ThrusterBlock.FACING, facing));
    }
}
