package com.ztok.create_sdt.compat.jei;

import com.ztok.create_sdt.CreateSmartDeployTorches;
import com.ztok.create_sdt.SDTBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;
import javax.annotation.Nonnull;

@JeiPlugin
@SuppressWarnings("null")
public class SDTJeiPlugin implements IModPlugin {

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CreateSmartDeployTorches.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistration registration) {
        // Registering subtypes as requested to ensure JEI handles the item correctly
        registration.registerSubtypeInterpreter(SDTBlocks.SMART_DEPLOYER.asItem(), new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(@Nonnull ItemStack stack, @Nonnull UidContext context) {
                return String.valueOf(stack.getComponents());
            }

            @Override
            public String getLegacyStringSubtypeInfo(@Nonnull ItemStack stack, @Nonnull UidContext context) {
                return String.valueOf(stack.getComponents());
            }
        });
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        // Manually adding the item to JEI in case it's missing from Creative Tabs
        registration.getIngredientManager().addIngredientsAtRuntime(
            VanillaTypes.ITEM_STACK,
            Collections.singletonList(SDTBlocks.SMART_DEPLOYER.asStack())
        );
    }
}
