package com.sporeadds.sporeaddsmod.util;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class KommandantFoodHelper {

    public static boolean isKommandant(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equals(data.getIdentifier()))
                .orElse(false);
    }

    public static boolean isAllowedKommandantFood(ItemStack stack) {
        if (stack.isEmpty() || !stack.isEdible()) {
            return false;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            return false;
        }

        List<? extends String> whitelist = SporeAddsConfig.KOMMANDANT_EDIBLE_ITEMS.get();
        return whitelist.contains(key.toString());
    }
}