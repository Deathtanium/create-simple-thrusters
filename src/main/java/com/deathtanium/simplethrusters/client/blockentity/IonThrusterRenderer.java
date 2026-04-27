package com.deathtanium.simplethrusters.client.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.deathtanium.simplethrusters.client.ThrustersPartials;
import com.deathtanium.simplethrusters.compat.CompatLoader;
import com.deathtanium.simplethrusters.content.thruster.IonThrusterBlock;
import com.deathtanium.simplethrusters.content.thruster.ThrusterBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class IonThrusterRenderer extends SafeBlockEntityRenderer<ThrusterBlockEntity> {

    public IonThrusterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(
            ThrusterBlockEntity be,
            float partialTicks,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay) {
        if (CompatLoader.createAdditionLoaded()) return;
        if (!(be.getBlockState().getBlock() instanceof IonThrusterBlock)) return;

        float fill = (float) be.getEnergyStoredForRender() / Math.max(1, be.getEnergyCapacityForRender());
        BlockState blockState = be.getBlockState();
        var vb = buffer.getBuffer(RenderType.solid());
        ms.pushPose();
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);
        msr.translate(new Vector3f(0.5f, 0.5f, 0.5f));

        float dialPivotY = 6f / 16f;
        float dialPivotZ = 8f / 16f;

        for (Direction d : Iterate.horizontalDirections) {
            ms.pushPose();
            CachedBuffers.partial(ThrustersPartials.FE_GAUGE_BG, blockState)
                    .rotateYDegrees(d.toYRot())
                    .uncenter()
                    .translate(0.5f - 6 / 16f, 0, 0)
                    .light(light)
                    .renderInto(ms, vb);
            CachedBuffers.partial(ThrustersPartials.FE_GAUGE_DIAL, blockState)
                    .rotateYDegrees(d.toYRot())
                    .uncenter()
                    .translate(0.5f - 6 / 16f, 0, 0)
                    .translate(0, dialPivotY, dialPivotZ)
                    .rotateXDegrees(-180 * fill)
                    .translate(0, -dialPivotY, -dialPivotZ)
                    .light(light)
                    .renderInto(ms, vb);
            ms.popPose();
        }

        ms.popPose();
    }
}
