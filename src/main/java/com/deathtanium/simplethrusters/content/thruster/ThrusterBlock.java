package com.deathtanium.simplethrusters.content.thruster;

import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public abstract class ThrusterBlock extends HorizontalDirectionalBlock implements IBE<ThrusterBlockEntity> {

    protected ThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public abstract ThrusterType thrusterType();

    public static Direction nozzleDirection(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction horizontal = ctx.getHorizontalDirection();
        Direction facing = ctx.isSecondaryUseActive() ? horizontal : horizontal.getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<ThrusterBlockEntity> getBlockEntityClass() {
        return ThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrusterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.THRUSTER.get();
    }

    public static BlockBehaviour.Properties applyProperties(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f).noOcclusion().pushReaction(PushReaction.BLOCK);
    }
}
