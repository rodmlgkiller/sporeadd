package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.particles.GoreParticleData;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import com.sporeadds.sporeaddsmod.util.DelayedTaskScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Random;

@EventBusSubscriber(modid = "sporeadd")
public final class BackstabEventHandler {

    private static final double BACKSTAB_MULTIPLIER = 2.0D;
    private static final double BACKSTAB_ANGLE_THRESHOLD = 0.5D;
    private static final double BACKSTAB_MAX_DISTANCE = 7.0D;
    private static final double CAMOUFLAGE_BONUS_MULTIPLIER = 1.5D;
    private static final int GORE_PARTICLE_COUNT = 7;
    private static final int CRITICAL_WOUND_DURATION_TICKS = 20 * 30;
    private static final int REPEAT_COUNT = 3;
    private static final int REPEAT_TICK_INTERVAL = 1;

    private static final float EXECUTION_HEALTH_THRESHOLD = 20.0F;

    private static final int BASE_EXECUTION_XP = 8;
    private static final int BUSH_HIT_XP = 20;
    private static final int BUSH_EXECUTION_XP = 12;

    private static final Random RANDOM = new Random();

    private BackstabEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }

        if (attacker == target) {
            return;
        }

        boolean isGhost = SporeIdentifierProvider.SPORE_IDENTIFIER.get(attacker)
                .map(data -> "ghost".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isGhost) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty() || weapon.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE).isEmpty()) {
            return;
        }

        if (attacker.distanceTo(target) > BACKSTAB_MAX_DISTANCE) {
            return;
        }

        if (!isBackstab(attacker, target)) {
            return;
        }

        boolean bushAbilityActive = SporeIdentifierProvider.SPORE_IDENTIFIER.get(attacker)
                .map(data -> data.isCamouflaged())
                .orElse(false);

        double totalMultiplier = BACKSTAB_MULTIPLIER;
        if (bushAbilityActive) {
            totalMultiplier *= CAMOUFLAGE_BONUS_MULTIPLIER;
        }

        float newAmount = event.getNewDamage() * (float) totalMultiplier;
        event.setNewDamage(newAmount);

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnGoreParticles(serverLevel, target);
        playBackstabSound(serverLevel, target);

        if (bushAbilityActive) {
            target.addEffect(new MobEffectInstance(
                    effects.CRITICAL_WOUND,
                    CRITICAL_WOUND_DURATION_TICKS,
                    0,
                    false,
                    true,
                    true
            ));

            for (int repeat = 1; repeat <= REPEAT_COUNT - 1; repeat++) {
                int delay = repeat * REPEAT_TICK_INTERVAL;
                DelayedTaskScheduler.schedule(delay, () -> {
                    if (!target.isAlive()) return;
                    spawnGoreParticles(serverLevel, target);
                    playBackstabSound(serverLevel, target);
                });
            }
        }

        boolean targetHasActiveArmor = target instanceof ServerPlayer targetPlayer
                && PlayerDataProvider.PLAYER_DATA.get(targetPlayer)
                .map(data -> data.getArmorHp() > 0)
                .orElse(false);

        boolean willExecute = !targetHasActiveArmor
                && (target.getHealth() - newAmount) < EXECUTION_HEALTH_THRESHOLD;

        int xpAmount;
        if (bushAbilityActive) {
            xpAmount = willExecute ? BUSH_EXECUTION_XP : BUSH_HIT_XP;
        } else {
            xpAmount = willExecute ? BASE_EXECUTION_XP : 0;
        }

        Vec3 xpPos = target.position();

        if (willExecute) {
            DelayedTaskScheduler.schedule(1, () -> {
                if (target.isAlive()) {
                    target.hurt(target.damageSources().genericKill(), Float.MAX_VALUE);
                }
            });
        }

        if (xpAmount > 0) {
            ExperienceOrb.award(serverLevel, xpPos, xpAmount);
        }
    }

    private static boolean isBackstab(ServerPlayer attacker, LivingEntity target) {
        Vec3 attackerLook = attacker.getLookAngle();
        Vec3 targetLook = target.getLookAngle();
        Vec3 attackerToTarget = target.position().subtract(attacker.position()).normalize();

        double facingSameDirection = attackerLook.dot(targetLook);
        double attackerBehindTarget = attackerToTarget.dot(targetLook);

        return facingSameDirection > BACKSTAB_ANGLE_THRESHOLD && attackerBehindTarget > BACKSTAB_ANGLE_THRESHOLD;
    }

    private static void spawnGoreParticles(ServerLevel level, LivingEntity target) {
        for (int i = 0; i < GORE_PARTICLE_COUNT; i++) {
            int variantIndex = 13 + RANDOM.nextInt(5);

            double dx = (RANDOM.nextDouble() - 0.5D) * 0.6D;
            double dy = RANDOM.nextDouble() * 0.4D;
            double dz = (RANDOM.nextDouble() - 0.5D) * 0.6D;

            double vx = dx * 2.0D;
            double vy = 0.2D + RANDOM.nextDouble() * 0.3D;
            double vz = dz * 2.0D;

            level.sendParticles(
                    new GoreParticleData(SporeaddParticleTypes.GORE.get(), variantIndex),
                    target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                    0,
                    vx, vy, vz,
                    1.0D
            );
        }
    }

    private static void playBackstabSound(ServerLevel level, LivingEntity target) {
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath("spore", "infected_weapon_hit_entity")),
                SoundSource.MASTER,
                1.0F,
                0.75F
        );
    }
}