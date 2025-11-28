package com.ztok.create_sdt.content.smart_deployer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.ztok.create_sdt.SDTPartialModels;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import com.mojang.math.Axis;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

@SuppressWarnings("null")
public class SmartDeployerActorVisual extends ActorVisual {

    private final TransformedInstance pole;
    private final TransformedInstance hand;
    private final RotatingInstance shaft;
    
    private final Matrix4f basePoleTransform;
    private final Matrix4f baseHandTransform;

    public SmartDeployerActorVisual(VisualizationContext visualizationContext, BlockAndTintGetter simulationWorld, MovementContext context) {
        super(visualizationContext, simulationWorld, context);

        BlockState state = context.state;
        Direction facing = state.getValue((net.minecraft.world.level.block.state.properties.Property<Direction>) FACING);

        // Shaft
        Direction.Axis axis = KineticBlockEntityVisual.rotationAxis(state);
        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
            .createInstance()
            .rotateToFace(axis);

        int blockLight = localBlockLight();

        shaft.setRotationAxis(axis)
            .setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, context.localPos))
            .setRotationalSpeed((float) context.getAnimationSpeed())
            .setPosition(context.localPos)
            .light(blockLight, 0)
            .setChanged();
        
        // Pole & Hand
        pole = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(SDTPartialModels.SMART_DEPLOYER_POLE))
            .createInstance();

        PartialModel handModel = SDTPartialModels.SMART_DEPLOYER_HAND_POINTING;
        if (context.blockEntityData != null && "PUNCH".equals(context.blockEntityData.getString("Mode"))) {
            handModel = SDTPartialModels.SMART_DEPLOYER_HAND_PUNCHING;
        }
        
        hand = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(handModel))
            .createInstance();

        // Calculate base rotation
        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        boolean rotatePole = state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        float zRot = rotatePole ? 90 : 0;

        basePoleTransform = new Matrix4f();
        basePoleTransform.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());
        basePoleTransform.translate(0.5f, 0.5f, 0.5f);
        basePoleTransform.rotate(Axis.YP.rotationDegrees(yRot));
        basePoleTransform.rotate(Axis.XP.rotationDegrees(xRot));
        basePoleTransform.rotate(Axis.ZP.rotationDegrees(zRot));
        basePoleTransform.translate(-0.5f, -0.5f, -0.5f);
        
        baseHandTransform = new Matrix4f(basePoleTransform);
        
        // Apply initial transform
        pole.setTransform(basePoleTransform)
            .light(blockLight)
            .setChanged();
            
        hand.setTransform(baseHandTransform)
            .light(blockLight)
            .setChanged();
    }

    @Override
    public void beginFrame() {
        float distance = deploymentDistance();

        float speed = (float) context.getAnimationSpeed();
        if (context.contraption.stalled) speed = 0;
        
        shaft.setRotationalSpeed(speed)
             .setChanged();

        Matrix4f poleTransform = new Matrix4f(basePoleTransform);
        poleTransform.translate(0, 0, distance);
        
        pole.setTransform(poleTransform)
            .setChanged();

        Matrix4f handTransform = new Matrix4f(baseHandTransform);
        handTransform.translate(0, 0, distance);
        
        hand.setTransform(handTransform)
            .setChanged();
    }

    private float deploymentDistance() {
        double factor;
        if (context.disabled) {
            factor = 0;
        } else if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3 center = VecHelper.getCenterOf(BlockPos.containing(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion)
                                                  .distanceTo(center);
            factor = .5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }
        return (float) factor;
    }

    @Override
    protected void _delete() {
        pole.delete();
        hand.delete();
        shaft.delete();
    }
}
