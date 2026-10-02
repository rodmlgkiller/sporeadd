package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncgluttonousCrosshairAttackPacket;
import com.sporeadds.sporeaddsmod.particles.GoreParticleData;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class gluttonousBiteAttackHandler {

    private static final ResourceLocation MEAT_ABOMINATION_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "meat_abomination");
    private static final ResourceLocation SCENT_ID = ResourceLocation.fromNamespaceAndPath("spore", "scent");

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        Entity target = event.getTarget();

        LazyOptional<SporeIdentifierData> cap = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER);
        boolean isgluttonous = cap.isPresent()
                && "gluttonous".equalsIgnoreCase(cap.orElseThrow(IllegalStateException::new).getSubclass());
        if (!isgluttonous) {
            return;
        }

        MobEffect faminedEffect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "famined"));
        if (faminedEffect == null || !player.hasEffect(faminedEffect)) {
            return;
        }

        float charge = player.getAttackStrengthScale(0.5F);
        if (charge < 1.0F) {
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (target.isInvulnerable() || SCENT_ID.equals(targetId)) {
            return;
        }

        player.resetAttackStrengthTicker();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            NetworkHandle.INSTANCE.send(
                    PacketDistributor.NEAR.with(() ->
                            new PacketDistributor.TargetPoint(
                                    player.getX(), player.getY(), player.getZ(), 32.0D, player.level().dimension()
                            )),
                    new SyncgluttonousCrosshairAttackPacket(player.getId())
            );

            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                    new SyncgluttonousCrosshairAttackPacket(player.getId())
            );

            float finalDamage = 30.0F;

            MobEffectInstance strength = player.getEffect(MobEffects.DAMAGE_BOOST);
            if (strength != null) {
                finalDamage += 3.0F * (strength.getAmplifier() + 1);
            }

            MobEffectInstance weakness = player.getEffect(MobEffects.WEAKNESS);
            if (weakness != null) {
                finalDamage -= 4.0F * (weakness.getAmplifier() + 1);
            }

            finalDamage = Math.max(0.0F, finalDamage);

            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = target.getZ();
            double targetMidY = targetY + (target.getBbHeight() / 2.0D);
            double targetWidth = target.getBbWidth();
            double targetHeight = target.getBbHeight();

            boolean success = livingTarget.hurt(player.damageSources().playerAttack(player), finalDamage);

            if (success) {
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 5, 0, false, false));

                boolean killedTarget = livingTarget.isDeadOrDying() || livingTarget.getHealth() <= 0;

                SoundEvent biteSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "sieger_bite"));
                if (biteSound != null) {
                    float pitch = 0.6F + (level.random.nextFloat() * 0.1F);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), biteSound, SoundSource.PLAYERS, 1.5F, pitch);
                }

                MobEffect mangledEffect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("sporeadd", "mangled"));
                int currentMangledLevel = 0;
                if (mangledEffect != null) {
                    MobEffectInstance currentEffect = livingTarget.getEffect(mangledEffect);
                    if (currentEffect != null) {
                        currentMangledLevel = currentEffect.getAmplifier() + 1;
                    }
                    livingTarget.addEffect(new MobEffectInstance(mangledEffect, 20 * 60 * 5, currentMangledLevel, false, true, true));
                }

                if (killedTarget) {
                    if (MEAT_ABOMINATION_ID.equals(targetId)) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 15 * 20, 1, false, true, true));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 1, false, true, true));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 15 * 20, 3, false, true, true));
                    }

                    int specialVariant = level.random.nextBoolean() ? 10 : 11;
                    serverLevel.sendParticles(
                            new GoreParticleData(SporeaddParticleTypes.GORE.get(), specialVariant),
                            targetX, targetMidY, targetZ,
                            1, 0.0D, 0.25D, 0.0D, 0.18D
                    );

                    int explosionCount = 40 + (currentMangledLevel * 2);
                    for (int i = 0; i < explosionCount; i++) {
                        int variant = level.random.nextFloat() < 0.2F
                                ? (6 + level.random.nextInt(4))
                                : (1 + level.random.nextInt(5));

                        double motionX = (level.random.nextDouble() - 0.5D) * 0.95D;
                        double motionY = level.random.nextDouble() * 0.95D;
                        double motionZ = (level.random.nextDouble() - 0.5D) * 0.95D;

                        serverLevel.sendParticles(
                                new GoreParticleData(SporeaddParticleTypes.GORE.get(), variant),
                                targetX, targetMidY, targetZ,
                                1, motionX, motionY, motionZ, 0.35D
                        );
                    }

                    SoundEvent evolveHurt = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "evolve_hurt"));
                    if (evolveHurt != null) {
                        serverLevel.playSound(null, targetX, targetY, targetZ, evolveHurt, SoundSource.HOSTILE, 1.0F, 0.5F);
                    }

                    SoundEvent hyperDamage = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("spore", "hyper_damage"));
                    if (hyperDamage != null) {
                        serverLevel.playSound(null, targetX, targetY, targetZ, hyperDamage, SoundSource.HOSTILE, 1.0F, 0.5F);
                    }

                    livingTarget.discard();

                } else {
                    int particleCount = 3 + (currentMangledLevel * 2);

                    for (int i = 0; i < particleCount; i++) {
                        int variant = 1 + level.random.nextInt(9);

                        double motionX = (level.random.nextDouble() - 0.5D) * 0.55D;
                        double motionY = level.random.nextDouble() * 0.45D;
                        double motionZ = (level.random.nextDouble() - 0.5D) * 0.55D;

                        serverLevel.sendParticles(
                                new GoreParticleData(SporeaddParticleTypes.GORE.get(), variant),
                                targetX + (level.random.nextDouble() - 0.5D) * targetWidth,
                                targetY + level.random.nextDouble() * targetHeight,
                                targetZ + (level.random.nextDouble() - 0.5D) * targetWidth,
                                1, motionX, motionY, motionZ, 0.20D
                        );
                    }
                }
            }
        }
    }
}