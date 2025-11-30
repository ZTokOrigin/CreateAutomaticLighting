package com.ztok.create_sdt;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.ztok.create_sdt.datagen.SDTRecipeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateSmartDeployTorches.MOD_ID)
public class CreateSmartDeployTorches {
    public static final String MOD_ID = "create_sdt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));

    public CreateSmartDeployTorches(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);

        REGISTRATE.registerEventListeners(modEventBus);

        SDTBlocks.register();
        SDTBlockEntities.register();
        if (FMLEnvironment.dist.isClient()) {
            try {
                Class.forName("com.ztok.create_sdt.ClientSetup").getMethod("init").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Register ourselves for server and other game events we are interested in
        // NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Create: Smart Deploy Torches - Initialized!");
    }

    private void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            new SDTRecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider())
        );
    }
}
