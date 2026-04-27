package com.deathtanium.simplethrusters.fluids;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * Non-placeable gas-like fluid (legacy block is air), matching Mekanism-style liquid gas behavior for open pipes.
 */
public class SimpleGasFluid extends BaseFlowingFluid {
    private final boolean source;

    public SimpleGasFluid(Properties properties, boolean source) {
        super(properties);
        this.source = source;
    }

    @Override
    public Fluid getSource() {
        return source ? this : super.getSource();
    }

    @Override
    public Fluid getFlowing() {
        return source ? super.getFlowing() : this;
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState state) {
        return source;
    }

    @Override
    public int getAmount(FluidState state) {
        return 0;
    }
}
