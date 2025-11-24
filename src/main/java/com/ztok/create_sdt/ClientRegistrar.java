package com.ztok.create_sdt;

import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.ztok.create_sdt.content.smart_deployer.SmartDeployerBlockEntity;

public class ClientRegistrar {
    @SuppressWarnings("unchecked")
    public static void registerRenderers(Object builderObj) {
        if (builderObj instanceof CreateBlockEntityBuilder) {
            CreateBlockEntityBuilder<SmartDeployerBlockEntity, ?> builder = (CreateBlockEntityBuilder<SmartDeployerBlockEntity, ?>) builderObj;
            builder.visual(() -> ClientRenderers::visual)
                   .renderer(() -> context -> ClientRenderers.renderer().create(context));
        }
    }
}
