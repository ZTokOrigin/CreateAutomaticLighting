package com.ztok.create_auto_light.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.ztok.create_auto_light.SDTBlockEntities;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class SmartDeployerBlock extends DeployerBlock {

    public SmartDeployerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends SmartDeployerBlockEntity> getBlockEntityType() {
        return SDTBlockEntities.SMART_DEPLOYER.get();
    }


}
