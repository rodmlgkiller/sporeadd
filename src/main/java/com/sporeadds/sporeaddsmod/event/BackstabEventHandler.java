package com.sporeadds.sporeaddsmod.event;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.abilities.GhostDisguiseStacks;
import com.sporeadds.sporeaddsmod.config.ConfigEffects;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Random;

@EventBusSubscriber(modid = "sporeadd")
public final class BackstabEventHandler {

    // The damage multipliers, distance, angle, execution rule, critical wound and XP numbers are configurable: see GhostConfig (ghost.* in the config).
    private static final int GORE_PARTICLE_COUNT = 7;
    private static final int REPEAT_COUNT = 3;
    private static final int REPEAT_TICK_INTERVAL = 1;

    private static final Random RANDOM = new Random();

    private BackstabEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingDamageEvent.Pre event) {

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
        if (weapon.isEmpty() || weapon.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
                .modifiers().stream().noneMatch(entry -> entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND))) {
            return;
        }

        if (attacker.distanceTo(target) > GhostConfig.backstabMaxDistance()) {
            return;
        }

        if (!isBackstab(attacker, target)) {
            return;
        }

        boolean bushAbilityActive = SporeIdentifierProvider.SPORE_IDENTIFIER.get(attacker)
                .map(data -> data.isCamouflaged())
                .orElse(false);

        double totalMultiplier = GhostConfig.backstabDamageMultiplier();
        if (bushAbilityActive) {
            totalMultiplier *= GhostConfig.disguiseBackstabBonusMultiplier();
        }

        float newAmount = event.getNewDamage() * (float) totalMultiplier;
        event.setNewDamage(newAmount);

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnGoreParticles(serverLevel, target);
        playBackstabSound(serverLevel, target);

        if (bushAbilityActive || !GhostConfig.CRITICAL_WOUND_ONLY_WHEN_DISGUISED.get()) {
            target.addEffect(new MobEffectInstance(
                    effects.CRITICAL_WOUND,
                    GhostConfig.criticalWoundDurationTicks(),
                    0,
                    false,
                    true,
                    true
            ));

            ConfigEffects.apply(target, ConfigEffects.parseAll(GhostConfig.CRITICAL_WOUND_EXTRA_EFFECTS.get()));
        }

        if (bushAbilityActive) {
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
                && (GhostConfig.EXECUTION_CAN_EXECUTE_PLAYERS.get() || !(target instanceof Player))
                && (target.getHealth() - newAmount) < GhostConfig.executionThreshold(target);

        int xpAmount;
        if (bushAbilityActive) {
            xpAmount = willExecute ? GhostConfig.BACKSTAB_XP_DISGUISED_EXECUTION.get() : GhostConfig.BACKSTAB_XP_DISGUISED_HIT.get();
        } else {
            xpAmount = willExecute ? GhostConfig.BACKSTAB_XP_EXECUTION.get() : 0;
        }

        Vec3 xpPos = target.position();

        if (willExecute) {
            DelayedTaskScheduler.schedule(1, () -> {
                if (target.isAlive()) {
                    target.hurt(target.damageSources().genericKill(), Float.MAX_VALUE);

                    // The execution has no attacker, so it is not seen by the kill trigger of the disguise stacks.
                    if (!target.isAlive()) {
                        GhostDisguiseStacks.consume(attacker, GhostConfig.StackConsume.KILL);
                    }
                }
            });
        }

        if (xpAmount > 0) {
            ExperienceOrb.award(serverLevel, xpPos, xpAmount);
        }

        GhostDisguiseStacks.consume(attacker, GhostConfig.StackConsume.BACKSTAB);
    }

    private static boolean isBackstab(ServerPlayer attacker, LivingEntity target) {
        Vec3 attackerLook = attacker.getLookAngle();
        Vec3 targetLook = target.getLookAngle();
        Vec3 attackerToTarget = target.position().subtract(attacker.position()).normalize();

        double facingSameDirection = attackerLook.dot(targetLook);
        double attackerBehindTarget = attackerToTarget.dot(targetLook);

        double angleThreshold = GhostConfig.backstabAngleThreshold();

        return facingSameDirection > angleThreshold && attackerBehindTarget > angleThreshold;
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