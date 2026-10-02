package com.sporeadds.sporeaddsmod.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Enchantments are data-driven in 1.21, so every lookup needs registry access. */
public final class EnchantUtil {

    private EnchantUtil() {
    }

    public static Holder<Enchantment> holder(HolderLookup.Provider access, ResourceKey<Enchantment> key) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    public static void add(ItemStack stack, HolderLookup.Provider access, ResourceKey<Enchantment> key, int level) {
        Holder<Enchantment> enchantment = holder(access, key);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }

    public static void remove(ItemStack stack, HolderLookup.Provider access, ResourceKey<Enchantment> key) {
        Holder<Enchantment> enchantment = holder(access, key);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, 0));
    }

    public static int level(ItemStack stack, HolderLookup.Provider access, ResourceKey<Enchantment> key) {
        return stack.getEnchantmentLevel(holder(access, key));
    }
}
