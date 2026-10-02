package com.sporeadds.jei;

import net.minecraft.world.item.ItemStack;

public class MedicBlockJeiRecipe {
    private final ItemStack input;
    private final ItemStack output;

    public MedicBlockJeiRecipe(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    public ItemStack getInput() { return input; }
    public ItemStack getOutput() { return output; }
}