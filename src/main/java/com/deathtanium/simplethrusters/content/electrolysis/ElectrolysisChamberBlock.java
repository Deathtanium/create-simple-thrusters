package com.deathtanium.simplethrusters.content.electrolysis;

import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

public class ElectrolysisChamberBlock extends Block implements IBE<ElectrolysisChamberBlockEntity> {

    public static final MapCodec<ElectrolysisChamberBlock> CODEC = simpleCodec(ElectrolysisChamberBlock::new);

    public enum Segment implements net.minecraft.util.StringRepresentable {
        BOTTOM,
        MIDDLE,
        TOP;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static final EnumProperty<Segment> SEGMENT = EnumProperty.create("segment", Segment.class);

    public ElectrolysisChamberBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SEGMENT, Segment.BOTTOM));
    }

    @Override
    protected MapCodec<? extends ElectrolysisChamberBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEGMENT);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ElectrolysisChamberBlockEntity be
                ? be.getComparatorOutput()
                : 0;
    }

    @Override
    public Class<ElectrolysisChamberBlockEntity> getBlockEntityClass() {
        return ElectrolysisChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectrolysisChamberBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTROLYSIS_CHAMBER.get();
    }

    public static BlockBehaviour.Properties applyProperties(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f).noOcclusion();
    }
}
