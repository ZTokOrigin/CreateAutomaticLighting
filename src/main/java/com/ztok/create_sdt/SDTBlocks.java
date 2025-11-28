package com.ztok.create_sdt;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.ztok.create_sdt.content.smart_deployer.SmartDeployerBlock;
import com.ztok.create_sdt.content.smart_deployer.SmartDeployerMovementBehaviour;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

@SuppressWarnings("null")
public class SDTBlocks {

    private static final CreateRegistrate REGISTRATE = CreateSmartDeployTorches.REGISTRATE;

    public static final BlockEntry<SmartDeployerBlock> SMART_DEPLOYER = REGISTRATE.block("smart_deployer", SmartDeployerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            // .onRegister(com.simibubi.create.content.kinetics.BlockStressDefaults.setImpact(4.0))
            .onRegister(MovementBehaviour.movementBehaviour(new SmartDeployerMovementBehaviour()))
            .blockstate((c, p) -> {})
            .item()
            .transform(customItemModel())
            .register();

    public static void register() {
        // load class
    }
}
