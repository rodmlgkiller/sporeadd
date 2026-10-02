package com.sporeadds.jei;

import net.minecraft.world.item.ItemStack;

public class MedicConstructorJeiRecipe {
    private final ItemStack input1;
    private final ItemStack input2;
    private final ItemStack output;

    public MedicConstructorJeiRecipe(ItemStack input1, ItemStack input2, ItemStack output) {
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
    }

    public ItemStack getInput1() { return input1; }
    public ItemStack getInput2() { return input2; }
    public ItemStack getOutput() { return output; }
}