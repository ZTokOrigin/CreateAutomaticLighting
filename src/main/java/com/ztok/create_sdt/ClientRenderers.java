package com.ztok.create_sdt;

import com.ztok.create_sdt.content.smart_deployer.SmartDeployerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class ClientRenderers {
    public static BlockEntityRendererProvider<SmartDeployerBlockEntity> renderer() {
        return context -> (net.minecraft.client.renderer.blockentity.BlockEntityRenderer<SmartDeployerBlockEntity>) (Object) new SmartDeployerRenderer(context);
    }

    public static SmartDeployerVisual visual(VisualizationContext context, SmartDeployerBlockEntity blockEntity, float partialTick) {
        return new SmartDeployerVisual(context, blockEntity, partialTick);
    }
}
