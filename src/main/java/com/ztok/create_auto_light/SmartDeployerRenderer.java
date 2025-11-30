package com.ztok.create_auto_light;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

@SuppressWarnings("null")
public class SmartDeployerRenderer extends DeployerRenderer {


    public SmartDeployerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("null")
    protected void renderComponents(DeployerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                                    int light, int overlay) {
        VertexConsumer vb = buffer.getBuffer(RenderType.solid());
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            KineticBlockEntityRenderer.renderRotatingKineticBlock(be, getRenderedBlockState(be), ms, vb, light);
        }

        BlockState blockState = be.getBlockState();
        Vec3 offset = getHandOffset(be, partialTicks, blockState);

        SuperByteBuffer pole = CachedBuffers.partial(SDTPartialModels.SMART_DEPLOYER_POLE, blockState);
        
        // Accessing protected/package-private members is now possible
        Object mode = DeployerAccess.getMode(be);
        boolean punching = mode != null && mode.toString().equals("PUNCH");
        PartialModel handPose = punching
            ? SDTPartialModels.SMART_DEPLOYER_HAND_PUNCHING 
            : SDTPartialModels.SMART_DEPLOYER_HAND_POINTING;
            
        SuperByteBuffer hand = CachedBuffers.partial(handPose, blockState);

        transform(pole.translate(offset.x, offset.y, offset.z), blockState, true)
            .light(light)
            .renderInto(ms, vb);
        transform(hand.translate(offset.x, offset.y, offset.z), blockState, false)
            .light(light)
            .renderInto(ms, vb);
    }

    private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
        Direction facing = deployerState.getValue(FACING);

        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        float zRot =
            axisDirectionMatters && (deployerState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90
                : 0;

        buffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        return buffer;
    }
}
