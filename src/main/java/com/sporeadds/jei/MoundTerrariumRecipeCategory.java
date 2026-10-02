package com.sporeadds.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MoundTerrariumRecipeCategory implements IRecipeCategory<MoundTerrariumJeiRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("sporeadd", "mound_terrarium");
    public static final mezz.jei.api.recipe.RecipeType<MoundTerrariumJeiRecipe> RECIPE_TYPE =
            mezz.jei.api.recipe.RecipeType.create("sporeadd", "mound_terrarium", MoundTerrariumJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public MoundTerrariumRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 92);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, SporeAddJeiPlugin.createMoundTerrariumStack());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<MoundTerrariumJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.sporeadds.mound_terrarium");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoundTerrariumJeiRecipe recipe, IFocusGroup focuses) {
        int terrariumX = 44;
        int toolX = 20;
        int inputY = 18;
        int firstOutputX = 82;
        int outputY = 15;

        if (!recipe.getInputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, toolX, inputY)
                    .addItemStack(recipe.getInputs().get(0));
        }

        if (recipe.getInputs().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, terrariumX, inputY)
                    .addItemStack(recipe.getInputs().get(1));
        }

        boolean isScentRecipe = recipe.getTitle().getString().equals(
                Component.translatable("jei.sporeadds.mound_terrarium.bottle").getString()
        );

        for (int i = 0; i < recipe.getOutputs().size(); i++) {
            int x = firstOutputX + (i % 5) * 18;
            int y = outputY + (i / 5) * 18;

            if (isScentRecipe && i == 0) {
                x = firstOutputX;
                y = 18;
            }

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(recipe.getOutputs().get(i));
        }
    }

    @Override
    public void draw(MoundTerrariumJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        guiGraphics.drawString(mc.font, recipe.getTitle(), 6, 0, 0x404040, false);
        guiGraphics.drawString(mc.font, "Tool", 20, 8, 0x808080, false);
        guiGraphics.drawString(mc.font, "Terrarium", 41, 8, 0x808080, false);
        guiGraphics.drawString(mc.font, "Results", 98, 8, 0x808080, false);

        guiGraphics.drawString(mc.font, ">", 74, 23, 0xFFFFFF, false);

        guiGraphics.drawWordWrap(mc.font, recipe.getDescription(), 6, 50, 164, 0x606060);
    }
}