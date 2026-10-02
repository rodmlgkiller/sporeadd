package com.sporeadds.sporeaddsmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Keeps the old "item NBT" call style on top of the CUSTOM_DATA data component.
 * getOrCreateTag hands out a tag owned by the stack (a fresh copy is installed on every call so that
 * stack copies and the last-synced inventory snapshot never share a mutable instance).
 */
public final class ItemNbt {

    private ItemNbt() {
    }

    public static boolean hasTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /** Read-only view (null when the stack has no custom data). Do not mutate; use {@link #getOrCreateTag}. */
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.getUnsafe();
    }

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CustomData fresh = CustomData.of(existing == null ? new CompoundTag() : existing.copyTag());
        stack.set(DataComponents.CUSTOM_DATA, fresh);
        return fresh.getUnsafe();
    }

    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (tag == null) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
