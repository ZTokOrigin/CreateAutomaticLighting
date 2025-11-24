package com.ztok.create_sdt;

import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class SDTPartialModels {

    public static final PartialModel 
        SMART_DEPLOYER_POLE = block("smart_deployer/pole"),
        SMART_DEPLOYER_HAND_POINTING = block("smart_deployer/hand_pointing"),
        SMART_DEPLOYER_HAND_PUNCHING = block("smart_deployer/hand_punching"),
        SMART_DEPLOYER_HAND_HOLDING = block("smart_deployer/hand_holding");

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateSmartDeployTorches.MOD_ID, "block/" + path));
    }

    public static void init() {
        // load class
    }
}
