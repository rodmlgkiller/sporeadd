package com.sporeadd.jei;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.jei.MedicBlockJeiRecipe;
import com.sporeadds.jei.SporeAddJeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MedicBlockRecipeCategory implements IRecipeCategory<MedicBlockJeiRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("sporeadd", "medic_category");
    private final IDrawable background;
    private final IDrawable icon;

    public MedicBlockRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(120, 50);
        this.icon = helper.createDrawableItemStack(new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "medic_block"))
        ));
    }

    @Override
    public RecipeType<MedicBlockJeiRecipe> getRecipeType() {
        return SporeAddJeiPlugin.MEDIC_BLOCK_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Medic Block");
    }

    @Override
    public IDrawable getBackground() { return this.background; }

    @Override
    public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MedicBlockJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 15).addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 15).addItemStack(recipe.getOutput());
    }
}