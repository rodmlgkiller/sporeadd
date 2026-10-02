package com.sporeadds.jei;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public class ScientistJeiRecipe {
    private final List<ItemStack> inputs;
    private final ItemStack output;

    public ScientistJeiRecipe(List<ItemStack> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output;
    }
}