package com.ztok.create_sdt;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateSmartDeployTorches.MOD_ID)
public class CreateSmartDeployTorches {
    public static final String MOD_ID = "create_sdt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CreateSmartDeployTorches(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        // NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Create: Smart Deploy Torches - Initialized!");
    }
}
