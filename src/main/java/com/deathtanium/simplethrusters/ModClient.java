package com.deathtanium.simplethrusters;

import com.deathtanium.simplethrusters.client.ThrustersPartials;
import com.deathtanium.simplethrusters.client.blockentity.IonThrusterRenderer;
import com.deathtanium.simplethrusters.content.thruster.ThrusterBlockEntity;
import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SimpleThrusters.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClient {
    private ModClient() {}

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        ThrustersPartials.init();
        event.registerBlockEntityRenderer(ModBlockEntities.THRUSTER.get(), IonThrusterRenderer::new);
    }
}
