package com.ztok.create_auto_light;

import com.ztok.create_auto_light.content.smart_deployer.SmartDeployerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class ClientRenderers {
    @SuppressWarnings("unchecked")
    public static BlockEntityRendererProvider<SmartDeployerBlockEntity> renderer() {
        return context -> (net.minecraft.client.renderer.blockentity.BlockEntityRenderer<SmartDeployerBlockEntity>) (Object) new SmartDeployerRenderer(context);
    }

    public static SmartDeployerVisual visual(VisualizationContext context, SmartDeployerBlockEntity blockEntity, float partialTick) {
        return new SmartDeployerVisual(context, blockEntity, partialTick);
    }
}
