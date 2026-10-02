package com.sporeadds.jei;

import com.sporeadds.jei.MedicConstructorJeiRecipe;
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
import net.minecraftforge.registries.ForgeRegistries;

public class MedicConstructorRecipeCategory implements IRecipeCategory<MedicConstructorJeiRecipe> {

    public static final ResourceLocation UID = new ResourceLocation("sporeadd", "medic_constructor_category");
    private final IDrawable background;
    private final IDrawable icon;

    public MedicConstructorRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(120, 50);
        this.icon = helper.createDrawableItemStack(new ItemStack(
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("sporeadd", "medic_constructor_block"))
        ));
    }

    @Override
    public RecipeType<MedicConstructorJeiRecipe> getRecipeType() {
        return SporeAddJeiPlugin.MEDIC_CONSTRUCTOR_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Medic Constructor");
    }

    @Override
    public IDrawable getBackground() { return this.background; }

    @Override
    public IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MedicConstructorJeiRecipe recipe, IFocusGroup focuses) {
        // Bloque de Redstone (Izquierda)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 15).addItemStack(recipe.getInput1());
        // Hierro (Medio)
        builder.addSlot(RecipeIngredientRole.INPUT, 35, 15).addItemStack(recipe.getInput2());
        // Medic Block (Derecha)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 15).addItemStack(recipe.getOutput());
    }
}