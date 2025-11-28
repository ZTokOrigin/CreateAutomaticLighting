package com.ztok.create_sdt.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.ztok.create_sdt.SDTBlockEntities;

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
