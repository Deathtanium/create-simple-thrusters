package com.deathtanium.simplethrusters.content.thruster;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CreativeThrusterBlock extends ThrusterBlock {
    public static final MapCodec<CreativeThrusterBlock> CODEC = simpleCodec(CreativeThrusterBlock::new);

    public CreativeThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ThrusterType thrusterType() {
        return ThrusterType.CREATIVE;
    }

    @Override
    protected MapCodec<? extends CreativeThrusterBlock> codec() {
        return CODEC;
    }
}
