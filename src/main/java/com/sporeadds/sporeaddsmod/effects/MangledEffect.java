package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.resources.ResourceLocation;

import com.sporeadds.sporeaddsmod.particles.GoreParticleData;
import com.sporeadds.sporeaddsmod.particles.SporeaddParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MangledEffect extends MobEffect {

    private static final ResourceLocation HEALTH_MOD_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "mangledeffect_health_mod_uuid");
    private static final ResourceLocation DAMAGE_MOD_UUID = ResourceLocation.fromNamespaceAndPath("sporeadd", "mangledeffect_damage_mod_uuid");
    private static final ResourceLocation SPEED_MOD_UUID  = ResourceLocation.fromNamespaceAndPath("sporeadd", "mangledeffect_speed_mod_uuid");
    private static final ResourceLocation STEP_MOD_UUID   = ResourceLocation.fromNamespaceAndPath("sporeadd", "mangledeffect_step_mod_uuid");

    private static final int MAX_PARTICLE_AMPLIFIER = 20;

    private static final int BLOOD_VARIANT_MIN = 13;
    private static final int BLOOD_VARIANT_MAX = 17;

    private static final double PARTICLE_COOLDOWN_MULTIPLIER = 0.10D;

    private static final int DOWNGRADE_THRESHOLD_TICKS = 20;
    private static final int DOWNGRADE_DURATION_TICKS = 20 * 60 * 5;

    public MangledEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A0303);
    }

    @Override
    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {
        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        removeAttributeModifiers(map);

        AttributeInstance health = map.getInstance(Attributes.MAX_HEALTH);
        if (health != null) {
            double healthPenalty = (amplifier + 1) * -3.0D;
            health.addPermanentModifier(new AttributeModifier(
                    HEALTH_MOD_UUID,
                    healthPenalty,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        AttributeInstance damage = map.getInstance(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            double damagePenalty = (amplifier + 1) * -0.10D;
            damage.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MOD_UUID,
                    damagePenalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        AttributeInstance speed = map.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            double speedPenalty = Math.max(-0.70D, (amplifier + 1) * -0.10D);
            speed.addPermanentModifier(new AttributeModifier(
                    SPEED_MOD_UUID,
                    speedPenalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        AttributeInstance step = map.getInstance(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        if (step != null) {
            step.addPermanentModifier(new AttributeModifier(
                    STEP_MOD_UUID,
                    1.0D,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap map) {
        AttributeInstance health = map.getInstance(Attributes.MAX_HEALTH);
        if (health != null) health.removeModifier(HEALTH_MOD_UUID);

        AttributeInstance damage = map.getInstance(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.removeModifier(DAMAGE_MOD_UUID);

        AttributeInstance speed = map.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SPEED_MOD_UUID);

        AttributeInstance step = map.getInstance(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        if (step != null) step.removeModifier(STEP_MOD_UUID);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        if (amplifier > 0 && duration <= DOWNGRADE_THRESHOLD_TICKS) {
            return true;
        }

        int effectiveAmplifier = Math.min(amplifier, MAX_PARTICLE_AMPLIFIER);

        double t = effectiveAmplifier / 20.0D;

        double minSeconds = Mth.lerp(t, 3.0D, 0.3D);
        double maxSeconds = Mth.lerp(t, 8.0D, 0.7D);

        int minTicks = Math.max(1, Mth.floor(minSeconds * 20.0D * PARTICLE_COOLDOWN_MULTIPLIER));
        int maxTicks = Math.max(minTicks, Mth.floor(maxSeconds * 20.0D * PARTICLE_COOLDOWN_MULTIPLIER));

        int interval = minTicks;
        if (maxTicks > minTicks) {
            interval += Math.abs(duration * 31 + amplifier * 17) % (maxTicks - minTicks + 1);
        }

        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
        if (current == null) {
            return true;
        }

        if (current.getAmplifier() > 0 && current.getDuration() <= DOWNGRADE_THRESHOLD_TICKS) {
            entity.removeEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
            entity.addEffect(new MobEffectInstance(
                    net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this),
                    DOWNGRADE_DURATION_TICKS,
                    current.getAmplifier() - 1,
                    current.isAmbient(),
                    current.isVisible(),
                    current.showIcon()
            ));
            return true;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return true;
        }

        AABB box = entity.getBoundingBox();
        int particleBursts = 1 + entity.getRandom().nextInt(3);

        for (int i = 0; i < particleBursts; i++) {
            double x = Mth.lerp(entity.getRandom().nextDouble(), box.minX, box.maxX);
            double y = Mth.lerp(entity.getRandom().nextDouble(), box.minY, box.maxY);
            double z = Mth.lerp(entity.getRandom().nextDouble(), box.minZ, box.maxZ);

            double vx = (entity.getRandom().nextDouble() - 0.5D) * 0.03D;
            double vy = 0.01D + entity.getRandom().nextDouble() * 0.03D;
            double vz = (entity.getRandom().nextDouble() - 0.5D) * 0.03D;

            int variant = BLOOD_VARIANT_MIN + entity.getRandom().nextInt(BLOOD_VARIANT_MAX - BLOOD_VARIANT_MIN + 1);

            serverLevel.sendParticles(
                    new GoreParticleData(SporeaddParticleTypes.GORE.get(), variant),
                    x,
                    y,
                    z,
                    1,
                    vx,
                    vy,
                    vz,
                    0.0D
            );
        }
        return true;
    }
}