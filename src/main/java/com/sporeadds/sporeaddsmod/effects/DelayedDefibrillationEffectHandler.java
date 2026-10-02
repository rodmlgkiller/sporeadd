package com.sporeadds.sporeaddsmod.effects;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd")
public final class DelayedDefibrillationEffectHandler {

    private static final int REVIVE_HEAL_DURATION_TICKS = 8;
    private static final double LIGHTNING_RADIUS = 4.0D;
    private static final float LIGHTNING_EXTRA_DAMAGE = 40.0F;
    private static final float KNOCKBACK_HORIZONTAL = 1.4F;
    private static final float KNOCKBACK_VERTICAL = 0.6F;
    private static final int POSTMORTEM_DURATION_TICKS = 20 * 150;
    private static final int MIN_INTERVAL_TICKS = 5;
    private static final int MAX_INTERVAL_TICKS = 40;
    private static final float SELF_DEFIBRILLATE_KILL_DAMAGE = 1000.0F;

    private static final Map<UUID, Long> NEXT_PARTICLE_TICK = new HashMap<>();
    private static final Map<UUID, Long> NEXT_SOUND_TICK = new HashMap<>();

    private DelayedDefibrillationEffectHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(effects.DELAYED_DEFIBRILLATION.get());

        if (effect == null) {
            NEXT_PARTICLE_TICK.remove(entity.getUUID());
            NEXT_SOUND_TICK.remove(entity.getUUID());
            return;
        }

        UUID uuid = entity.getUUID();
        long now = serverLevel.getGameTime();
        long nextTick = NEXT_PARTICLE_TICK.getOrDefault(uuid, 0L);

        if (now < nextTick) {
            return;
        }

        spawnElectricityParticles(serverLevel, entity);
        playElectricSound(serverLevel, entity);

        int interval = MIN_INTERVAL_TICKS
                + entity.getRandom().nextInt(MAX_INTERVAL_TICKS - MIN_INTERVAL_TICKS + 1);

        NEXT_PARTICLE_TICK.put(uuid, now + interval);
    }

    private static void spawnLightningImpactParticles(ServerLevel level, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5D;
        double z = entity.getZ();

        double width = entity.getBbWidth() * 0.9D;
        double height = entity.getBbHeight() * 0.6D;

        level.sendParticles(
                com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes.ELECTRICITY.get(),
                x, y, z,
                90,
                width, height, width,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                120,
                width, height, width,
                0.35D
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                70,
                width * 0.8D, height * 0.8D, width * 0.8D,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y, z,
                35,
                width * 0.6D, height * 0.9D, width * 0.6D,
                0.02D
        );
    }

    private static void spawnElectricityParticles(ServerLevel level, LivingEntity entity) {
        double baseX = entity.getX();
        double baseY = entity.getY();
        double baseZ = entity.getZ();
        double height = entity.getBbHeight();

        double motionX = entity.getDeltaMovement().x;
        double motionY = entity.getDeltaMovement().y;
        double motionZ = entity.getDeltaMovement().z;

        double lead = 0.35D;

        double[][] offsets = {
                {0.0D, height * 0.9D, 0.0D},
                {entity.getBbWidth() * 0.3D, height * 0.5D, 0.0D},
                {-entity.getBbWidth() * 0.3D, height * 0.5D, 0.0D},
                {0.0D, height * 0.1D, entity.getBbWidth() * 0.3D},
                {0.0D, height * 0.1D, -entity.getBbWidth() * 0.3D}
        };

        for (double[] offset : offsets) {
            level.sendParticles(
                    com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes.ELECTRICITY.get(),
                    baseX + offset[0] + motionX * lead,
                    baseY + offset[1] + motionY * lead,
                    baseZ + offset[2] + motionZ * lead,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }

    private static void playElectricSound(ServerLevel level, LivingEntity entity) {
        net.minecraft.sounds.SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath("spore", "electric")
        );

        if (sound == null) return;

        level.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                sound,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        MobEffectInstance effect = entity.getEffect(effects.DELAYED_DEFIBRILLATION.get());
        if (effect == null) return;

        if (event.getAmount() < entity.getHealth()) return;

        event.setCanceled(true);
        forceRevive(serverLevel, entity);
    }

    private static void triggerLightningImpact(ServerLevel level, LivingEntity entity) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.setPos(entity.getX(), entity.getY(), entity.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        net.minecraft.sounds.SoundEvent dischargeSound = BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath("spore", "electric_discharge")
        );

        if (dischargeSound != null) {
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    dischargeSound,
                    SoundSource.HOSTILE,
                    6.0F,
                    1.0F
            );
        }

        spawnLightningImpactParticles(level, entity);

        AABB radiusBox = entity.getBoundingBox().inflate(LIGHTNING_RADIUS);
        List<Entity> nearby = level.getEntities(entity, radiusBox);

        for (Entity target : nearby) {
            if (target == entity) continue;

            double distSq = target.distanceToSqr(entity);
            if (distSq > LIGHTNING_RADIUS * LIGHTNING_RADIUS) continue;

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.hurt(
                        entity.damageSources().lightningBolt(),
                        LIGHTNING_EXTRA_DAMAGE
                );

                applyKnockbackFrom(entity, livingTarget);
            }
        }
    }

    private static void applyKnockbackFrom(LivingEntity source, LivingEntity target) {
        Vec3 direction = target.position().subtract(source.position());
        double horizontalDist = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

        double normX = horizontalDist > 0.0001D ? direction.x / horizontalDist : 0.0D;
        double normZ = horizontalDist > 0.0001D ? direction.z / horizontalDist : 0.0D;

        target.setDeltaMovement(
                normX * KNOCKBACK_HORIZONTAL,
                KNOCKBACK_VERTICAL,
                normZ * KNOCKBACK_HORIZONTAL
        );

        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    public static void triggerSharedLightningImpact(ServerLevel level, LivingEntity entity) {
        triggerLightningImpact(level, entity);
    }

    public static void forceRevive(ServerLevel level, LivingEntity entity) {
        entity.removeEffect(effects.DELAYED_DEFIBRILLATION.get());
        NEXT_PARTICLE_TICK.remove(entity.getUUID());
        NEXT_SOUND_TICK.remove(entity.getUUID());

        entity.addEffect(new MobEffectInstance(
                MobEffects.HEAL,
                REVIVE_HEAL_DURATION_TICKS,
                4,
                false,
                true,
                true
        ));

        triggerLightningImpact(level, entity);
        applyPostmortem(entity);
    }

    public static void forceKillByLightning(ServerLevel level, LivingEntity entity) {
        NEXT_PARTICLE_TICK.remove(entity.getUUID());
        NEXT_SOUND_TICK.remove(entity.getUUID());

        triggerLightningImpact(level, entity);

        entity.hurt(
                entity.damageSources().lightningBolt(),
                SELF_DEFIBRILLATE_KILL_DAMAGE
        );
    }

    private static void applyPostmortem(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                effects.POSTMORTEM.get(),
                POSTMORTEM_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }
}