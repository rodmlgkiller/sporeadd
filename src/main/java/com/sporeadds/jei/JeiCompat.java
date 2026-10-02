package com.sporeadds.jei;

import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class JeiCompat {

    public static ItemStack createMoundTerrariumStack() {
        ItemStack stack = new ItemStack(modblocks.MOUND_TERRARIUM.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("HasMound", true);
        tag.putBoolean("Linked", false);
        tag.putInt("CustomModelData", 1);
        return stack;
    }
}