package com.deathtanium.simplethrusters.content.thruster;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlazerThrusterBlock extends ThrusterBlock {
    public static final MapCodec<BlazerThrusterBlock> CODEC = simpleCodec(BlazerThrusterBlock::new);

    public BlazerThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ThrusterType thrusterType() {
        return ThrusterType.BLAZER;
    }

    @Override
    protected MapCodec<? extends BlazerThrusterBlock> codec() {
        return CODEC;
    }
}
