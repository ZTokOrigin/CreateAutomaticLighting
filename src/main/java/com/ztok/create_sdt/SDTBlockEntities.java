package com.ztok.create_sdt;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.ztok.create_sdt.content.smart_deployer.SmartDeployerBlockEntity;

public class SDTBlockEntities {

    private static final CreateRegistrate REGISTRATE = CreateSmartDeployTorches.REGISTRATE;

    public static final BlockEntityEntry<SmartDeployerBlockEntity> SMART_DEPLOYER = registerSmartDeployer();

    private static BlockEntityEntry<SmartDeployerBlockEntity> registerSmartDeployer() {
        var builder = REGISTRATE
            .blockEntity("smart_deployer", SmartDeployerBlockEntity::new)
            .validBlocks(SDTBlocks.SMART_DEPLOYER);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            try {
                Class<?> clazz = Class.forName("com.ztok.create_sdt.ClientRegistrar");
                java.lang.reflect.Method method = clazz.getMethod("registerRenderers", Object.class);
                method.invoke(null, builder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return builder.register();
    }

    public static void register() {
        // load class
    }
}
