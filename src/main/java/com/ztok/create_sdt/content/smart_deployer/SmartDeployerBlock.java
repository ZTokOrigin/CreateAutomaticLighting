package com.ztok.create_sdt.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.ztok.create_sdt.SDTBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

public class SmartDeployerBlock extends DeployerBlock {

    public SmartDeployerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends SmartDeployerBlockEntity> getBlockEntityType() {
        return SDTBlockEntities.SMART_DEPLOYER.get();
    }


}
