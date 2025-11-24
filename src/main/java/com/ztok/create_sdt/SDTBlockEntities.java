package com.ztok.create_sdt;

import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import com.simibubi.create.content.kinetics.deployer.DeployerVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.ztok.create_sdt.content.smart_deployer.SmartDeployerBlockEntity;

public class SDTBlockEntities {

    private static final CreateRegistrate REGISTRATE = CreateSmartDeployTorches.REGISTRATE;

    public static final BlockEntityEntry<SmartDeployerBlockEntity> SMART_DEPLOYER = REGISTRATE
            .blockEntity("smart_deployer", SmartDeployerBlockEntity::new)
            .visual(() -> SmartDeployerVisual::new)
            .validBlocks(SDTBlocks.SMART_DEPLOYER)
            .renderer(() -> SmartDeployerRenderer::new)
            .register();

    public static void register() {
        // load class
    }
}
