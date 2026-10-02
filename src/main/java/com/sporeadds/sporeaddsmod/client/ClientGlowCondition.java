package com.sporeadds.sporeaddsmod.client;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public class ClientGlowCondition {

    public static boolean shouldSeeAsPurpleGlow(Player targetPlayer) {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;

        // Comprobaciones de seguridad básicas
        if (localPlayer == null || localPlayer == targetPlayer) return false;

        // 1. El jugador local debe estar en el equipo "spore"
        if (localPlayer.getTeam() == null || !localPlayer.getTeam().getName().equals("spore")) return false;

        // 2. El objetivo debe tener el efecto "spore:uneasy"
        MobEffect uneasy = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("spore", "uneasy"));
        if (uneasy == null || !targetPlayer.hasEffect(uneasy)) return false;

        // 3. Debe estar a 100 bloques o menos
        if (localPlayer.distanceTo(targetPlayer) > 100.0f) return false;

        // 4. El jugador local debe ser nivel 7 o superior
        return localPlayer.getCapability(PlayerLevelProvider.PLAYER_LVL)
                .map(lvl -> lvl.getLevel() >= 7)
                .orElse(false);
    }
}