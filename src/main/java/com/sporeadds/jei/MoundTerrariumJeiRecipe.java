package com.sporeadds.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MoundTerrariumJeiRecipe {

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private final Component title;
    private final Component description;

    public MoundTerrariumJeiRecipe(List<ItemStack> inputs, List<ItemStack> outputs, Component title, Component description) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.title = title;
        this.description = description;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public Component getTitle() {
        return title;
    }

    public Component getDescription() {
        return description;
    }
}