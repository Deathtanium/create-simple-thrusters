package com.deathtanium.simplethrusters.registry;

import com.deathtanium.simplethrusters.SimpleThrusters;
import com.deathtanium.simplethrusters.content.electrolysis.ElectrolysisChamberBlock;
import com.deathtanium.simplethrusters.content.thruster.BlazerThrusterBlock;
import com.deathtanium.simplethrusters.content.thruster.CreativeThrusterBlock;
import com.deathtanium.simplethrusters.content.thruster.IonThrusterBlock;
import com.deathtanium.simplethrusters.content.thruster.ThrusterBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.neoforged.bus.api.IEventBus;

public final class ModBlocks {
    private static final SimpleThrustersRegistrate REGISTRATE =
            SimpleThrustersRegistrate.create(SimpleThrusters.MOD_ID);

    public static final BlockEntry<CreativeThrusterBlock> CREATIVE_THRUSTER =
            REGISTRATE.block("creative_thruster", CreativeThrusterBlock::new)
                    .properties(ThrusterBlock::applyProperties)
                    .simpleItem()
                    .register();

    public static final BlockEntry<IonThrusterBlock> ION_THRUSTER =
            REGISTRATE.block("ion_thruster", IonThrusterBlock::new)
                    .properties(ThrusterBlock::applyProperties)
                    .simpleItem()
                    .register();

    public static final BlockEntry<BlazerThrusterBlock> BLAZER_THRUSTER =
            REGISTRATE.block("blazer_thruster", BlazerThrusterBlock::new)
                    .properties(ThrusterBlock::applyProperties)
                    .simpleItem()
                    .register();

    public static final BlockEntry<ElectrolysisChamberBlock> ELECTROLYSIS_CHAMBER =
            REGISTRATE.block("electrolysis_chamber", ElectrolysisChamberBlock::new)
                    .properties(ElectrolysisChamberBlock::applyProperties)
                    .simpleItem()
                    .register();

    private ModBlocks() {}

    public static void register(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);
        ModBlockEntities.register(modEventBus);
    }
}
