package com.sporeadds.sporeaddsmod.abilities;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class GhostCamouflageAbility {

    private GhostCamouflageAbility() {
    }

    private static boolean isCamouflaged(ServerPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(SporeIdentifierData::isCamouflaged)
                .orElse(false);
    }

    private static ServerPlayer resolveAttacker(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();

        if (source instanceof ServerPlayer player) {
            return player;
        }

        Entity direct = event.getSource().getDirectEntity();

        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }

        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurtRangedBonus(LivingDamageEvent.Pre event) {

        ServerPlayer attacker = resolveAttacker(event);

        if (attacker == null) {
            return;
        }

        if (!isCamouflaged(attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        double distance = attacker.distanceTo(target);

        if (distance <= GhostConfig.disguiseRangedMinDistance()) {
            return;
        }

        event.setNewDamage(event.getNewDamage() * GhostConfig.disguiseRangedDamageMultiplier());
    }
}