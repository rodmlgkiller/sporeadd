package com.sporeadds.jei;

import net.minecraft.core.registries.BuiltInRegistries;

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
import java.util.List;

public class ScientistRecipeCategory implements IRecipeCategory<ScientistJeiRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("sporeadd", "scientist_category");
    private final IDrawable background;
    private final IDrawable icon;

    public ScientistRecipeCategory(IGuiHelper helper) {
        // Hacemos el fondo un poco más ancho (140) para acomodar más ítems de biomasa
        this.background = helper.createBlankDrawable(140, 50);
        this.icon = helper.createDrawableItemStack(new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "scientist_block"))
        ));
    }

    @Override
    public RecipeType<ScientistJeiRecipe> getRecipeType() {
        return SporeAddJeiPlugin.SCIENTIST_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Research unit");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ScientistJeiRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.getInputs();

        // Creamos hasta 3 slots de entrada dependiendo de cuántos stacks de biomasa tenga la receta
        // X=10, X=28, X=46
        int xPos = 10;
        for (ItemStack inputStack : inputs) {
            builder.addSlot(RecipeIngredientRole.INPUT, xPos, 15).addItemStack(inputStack);
            xPos += 18; // Separación estándar entre slots en Minecraft
        }

        // Slot de salida (lo movemos un poco a la derecha, X=100)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 15)
                .addItemStack(recipe.getOutput());
    }
}