package com.ztok.create_auto_light.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;

import java.util.List;

public class SmartDeployerBlockEntity extends DeployerBlockEntity {

    protected ScrollValueBehaviour lightLevel;

    public SmartDeployerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        
        // Remove the default filtering behaviour (we don't want the filter slot)
        behaviours.remove(filtering);

        // Add our Light Level scroll behaviour
        lightLevel = new ScrollValueBehaviour(Component.literal("Light Level"), this, new SmartDeployerValueBox()) {
            @Override
            public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
                super.write(nbt, registries, clientPacket);
                nbt.putInt("LightLevel", value);
            }

            @Override
            public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
                super.read(nbt, registries, clientPacket);
                if (nbt.contains("LightLevel"))
                    value = nbt.getInt("LightLevel");
            }
        };
        lightLevel.between(0, 15);
        lightLevel.value = 7; // Default value
        behaviours.add(lightLevel);
    }

    private static class SmartDeployerValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 15.5);
        }

        @Override
        @SuppressWarnings("null")
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(DeployerBlock.FACING);
            Vec3 vec = VecHelper.voxelSpace(8f, 8f, 15.5f);

            vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Axis.Y);
            vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Axis.X);
            vec = vec.subtract(Vec3.atLowerCornerOf(facing.getNormal())
                .scale(2 / 16f));

            return vec;
        }

        @Override
        @SuppressWarnings("null")
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(DeployerBlock.FACING);
            if (direction.getAxis() == facing.getAxis())
                return false;
            if (((DeployerBlock) state.getBlock()).getRotationAxis(state) == direction.getAxis())
                return false;
            return true;
        }
    }

    public String getModeName() {
        return ((Enum<?>) this.mode).name();
    }
}
