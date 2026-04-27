package com.deathtanium.simplethrusters.content.thruster;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class IonThrusterBlock extends ThrusterBlock {
    public static final MapCodec<IonThrusterBlock> CODEC = simpleCodec(IonThrusterBlock::new);

    public IonThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ThrusterType thrusterType() {
        return ThrusterType.ION;
    }

    @Override
    protected MapCodec<? extends IonThrusterBlock> codec() {
        return CODEC;
    }
}
