package com.ztok.create_auto_light.datagen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.ztok.create_auto_light.SDTBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class SDTRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SDTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    @SuppressWarnings("null")
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SDTBlocks.SMART_DEPLOYER.get())
                .pattern("T")
                .pattern("D")
                .pattern("E")
                .define('T', Items.TORCH)
                .define('D', AllBlocks.DEPLOYER.get())
                .define('E', AllItems.ELECTRON_TUBE.get())
                .unlockedBy("has_deployer", has(AllBlocks.DEPLOYER.get()))
                .unlockedBy("has_electron_tube", has(AllItems.ELECTRON_TUBE.get()))
                .save(output);
    }
}
