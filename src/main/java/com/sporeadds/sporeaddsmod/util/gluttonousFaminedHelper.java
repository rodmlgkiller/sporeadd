package com.sporeadds.sporeaddsmod.util;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public class gluttonousFaminedHelper {

    public static boolean hasgluttonousFaminedAttack(Player player) {
        if (player == null) return false;

        boolean isgluttonous = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(SporeIdentifierData::getSubclass)
                .map(subclass -> "gluttonous".equalsIgnoreCase(subclass))
                .orElse(false);

        if (!isgluttonous) return false;

        MobEffect famined = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined"));
        return famined != null && player.hasEffect(famined);
    }
}