package com.ztok.create_sdt.datagen;

import com.ztok.create_sdt.SDTBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class SDTRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SDTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SDTBlocks.SMART_DEPLOYER.get())
                .pattern("T")
                .pattern("D")
                .pattern("E")
                .define('T', Items.TORCH)
                .define('D', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:deployer")))
                .define('E', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:electron_tube")))
                .unlockedBy("has_deployer", has(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:deployer"))))
                .save(output);
    }
}
