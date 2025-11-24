package com.ztok.create_sdt;

import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.ztok.create_sdt.SDTPartialModels;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class SmartDeployerVisual extends ShaftVisual<DeployerBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    final Direction facing;
    final float yRot;
    final float xRot;
    final float zRot;

    protected final OrientedInstance pole;
    protected OrientedInstance hand;

    PartialModel currentHand;
    float progress;

    public SmartDeployerVisual(VisualizationContext context, DeployerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        facing = blockState.getValue(FACING);

        boolean rotatePole = blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

        pole = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SDTPartialModels.SMART_DEPLOYER_POLE))
            .createInstance();

        // Accessing package-private Mode
        Object mode = DeployerAccess.getMode(blockEntity);
        currentHand = getHandPose(mode);

        hand = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(currentHand))
            .createInstance();

        progress = getProgress(partialTick);
        updateRotation(pole, hand, yRot, xRot, zRot);
        updatePosition();
    }
    
    static PartialModel getHandPose(Object mode) {
        boolean punching = mode != null && mode.toString().equals("PUNCH");
        return punching ? SDTPartialModels.SMART_DEPLOYER_HAND_PUNCHING : SDTPartialModels.SMART_DEPLOYER_HAND_POINTING;
    }

    @Override
    public void tick(TickableVisual.Context context) {
        super.tick(context);
        Object mode = DeployerAccess.getMode(blockEntity);
        PartialModel newHand = getHandPose(mode);

        if (currentHand != newHand) {
            currentHand = newHand;
            hand.delete();
            hand = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(currentHand))
                .createInstance();
            updateRotation(pole, hand, yRot, xRot, zRot);
            updatePosition();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float newProgress = getProgress(ctx.partialTick());
        if (Mth.equal(newProgress, progress))
            return;

        progress = newProgress;
        updatePosition();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pole, hand);
    }

    @Override
    protected void _delete() {
        super._delete();
        pole.delete();
        hand.delete();
    }

    private float getProgress(float partialTicks) {
        return blockEntity.getHandOffset(partialTicks);
    }

    private void updatePosition() {
        var pos = blockEntity.getBlockPos();
        float x = pos.getX() + facing.getStepX() * progress;
        float y = pos.getY() + facing.getStepY() * progress;
        float z = pos.getZ() + facing.getStepZ() * progress;
        
        pole.position(x, y, z).setChanged();
        hand.position(x, y, z).setChanged();
    }
    
    private void updateRotation(OrientedInstance pole, OrientedInstance hand, float yRot, float xRot, float zRot) {
        var rotation = Axis.YP.rotationDegrees(yRot)
            .mul(Axis.XP.rotationDegrees(xRot))
            .mul(Axis.ZP.rotationDegrees(zRot));

        pole.rotation(rotation).setChanged();
        hand.rotation(rotation).setChanged();
    }
}
