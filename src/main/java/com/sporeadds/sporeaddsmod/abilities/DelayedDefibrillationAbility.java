package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DelayedDefibrillationAbility {

    public static final int COOLDOWN_TICKS = 20 * 80;
    private static final double SEARCH_RADIUS = 4.0D;
    private static final int EFFECT_DURATION_TICKS = 300;

    private static final Map<UUID, Long> COOLDOWN_UNTIL_TICK = new HashMap<>();

    private DelayedDefibrillationAbility() {
    }

    public static long getCooldownRemainingTicks(ServerPlayer player) {
        long until = COOLDOWN_UNTIL_TICK.getOrDefault(player.getUUID(), 0L);
        long remaining = until - player.level().getGameTime();
        return Math.max(0L, remaining);
    }

    public static boolean tryActivate(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (getCooldownRemainingTicks(player) > 0) {
            return false;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        LivingEntity target = findTarget(serverLevel, player);

        if (target == null) {
            player.sendSystemMessage(
                    Component.translatable("ability.sporeadds.medic.no_available_targets")
                            .withStyle(ChatFormatting.RED)
            );
            return false;
        }

        applyEffect(serverLevel, target);

        long cooldownUntil = player.level().getGameTime() + COOLDOWN_TICKS;
        COOLDOWN_UNTIL_TICK.put(uuid, cooldownUntil);

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.sendTo(
                new com.sporeadds.sporeaddsmod.network.SyncDelayedDefibrillationCooldownPacket(COOLDOWN_TICKS),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );

        return true;
    }

    private static LivingEntity findTarget(ServerLevel level, ServerPlayer user) {
        AABB searchBox = user.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != user && isWithinRadius(user, entity, SEARCH_RADIUS)
        );

        ServerPlayer bestPlayer = null;
        double lowestHealth = Double.MAX_VALUE;

        for (LivingEntity entity : nearby) {
            if (!(entity instanceof ServerPlayer candidate)) continue;
            if (isKommandant(candidate)) continue;

            if (candidate.getHealth() < lowestHealth) {
                lowestHealth = candidate.getHealth();
                bestPlayer = candidate;
            }
        }

        if (bestPlayer != null) {
            return bestPlayer;
        }

        for (LivingEntity entity : nearby) {
            if (entity instanceof ServerPlayer) continue;
            if (isOnSporeTeam(entity)) continue;
            return entity;
        }

        return null;
    }

    private static boolean isWithinRadius(ServerPlayer user, LivingEntity entity, double radius) {
        return entity.distanceToSqr(user) <= radius * radius;
    }

    private static boolean isKommandant(ServerPlayer player) {
        return player.getCapability(com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isOnSporeTeam(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId != null
                && "spore".equals(entityId.getNamespace())
                && "conductor".equals(entityId.getPath())) {
            return true;
        }

        Team team = entity.getTeam();
        if (team instanceof PlayerTeam playerTeam) {
            return "spore".equalsIgnoreCase(playerTeam.getName());
        }

        return false;
    }

    private static void applyEffect(ServerLevel level, LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                effects.DELAYED_DEFIBRILLATION.get(),
                EFFECT_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));

        spawnApplicationParticles(level, target);
        playApplicationSound(level, target);
    }

    private static void spawnApplicationParticles(ServerLevel level, LivingEntity target) {
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                60,
                target.getBbWidth() * 0.5D,
                target.getBbHeight() * 0.5D,
                target.getBbWidth() * 0.5D,
                0.15D
        );
    }

    private static void playApplicationSound(ServerLevel level, LivingEntity target) {
        net.minecraft.sounds.SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(
                new ResourceLocation("spore", "electric_spark")
        );

        if (sound == null) return;

        level.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                sound,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}