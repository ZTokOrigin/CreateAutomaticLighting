package com.ztok.create_sdt.content.smart_deployer;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SmartDeployerVisuals {
    @OnlyIn(Dist.CLIENT)
    public static ActorVisual create(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new SmartDeployerActorVisual(visualizationContext, simulationWorld, movementContext);
    }
}
