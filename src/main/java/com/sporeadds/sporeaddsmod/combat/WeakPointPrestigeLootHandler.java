package com.sporeadds.sporeaddsmod.combat;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.research.PrestigeManager;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd")
public final class WeakPointPrestigeLootHandler {

    private static final DustParticleOptions GOLD_DUST =
            new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.84F, 0.0F), 1.0F);

    private WeakPointPrestigeLootHandler() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }

        String entityId;
        boolean isKommandantTarget = false;

        if (target instanceof ServerPlayer targetPlayer) {
            boolean isKommandant = targetPlayer.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                    .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                    .orElse(false);

            if (!isKommandant) {
                return;
            }

            entityId = TrackedEntities.KOMMANDANT_PLAYER_ID;
            isKommandantTarget = true;
        } else {
            entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
            if (!TrackedEntities.ENTITY_IDS.contains(entityId)) {
                return;
            }
        }

        UUID attackerId = isKommandantTarget
                ? WeakPointKillTracker.getRealKillAttacker(target.getId())
                : attacker.getUUID();

        if (attackerId == null) {
            return;
        }

        boolean killedByWeakPoint = WeakPointKillTracker.wasKilledByWeakPoint(
                target.getId(), attackerId, serverLevel.getGameTime()
        );

        if (!killedByWeakPoint) {
            return;
        }

        if (!PrestigeManager.isPrestigedByAnyScientist(serverLevel.getServer(), entityId)) {
            return;
        }

        spawnPrestigeParticles(serverLevel, target);

        serverLevel.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.5F,
                1.6F
        );
    }

    private static void spawnPrestigeParticles(ServerLevel serverLevel, LivingEntity target) {
        for (int i = 0; i < 10; i++) {
            double rx = (Math.random() - 0.5) * target.getBbWidth() * 1.5;
            double ry = Math.random() * target.getBbHeight();
            double rz = (Math.random() - 0.5) * target.getBbWidth() * 1.5;

            serverLevel.sendParticles(
                    GOLD_DUST,
                    target.getX() + rx,
                    target.getY() + ry,
                    target.getZ() + rz,
                    1, 0.0D, 0.02D, 0.0D, 0.0D
            );
        }
    }
}