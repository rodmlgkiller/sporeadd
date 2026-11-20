package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

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

        // biomass_from_eating.food_to_biomass: every food is allowed.
        if (KommandantPowersConfig.FOOD_BIOMASS_ENABLED.get()) {
            return true;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return false;
        }

        List<? extends String> whitelist = SporeAddsConfig.KOMMANDANT_EDIBLE_ITEMS.get();
        return whitelist.contains(key.toString());
    }
}