package com.sporeadds.sporeaddsmod.effects;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class MangledEffectHandler {

    private static final int MAX_EFFECTIVE_AMPLIFIER = 15;

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        MobEffectInstance mangled = entity.getEffect(effects.MANGLED);
        if (mangled == null) return;

        int amplifier = Math.min(mangled.getAmplifier(), MAX_EFFECTIVE_AMPLIFIER);

        double reductionPerLevel = 0.10D;
        double totalReduction = reductionPerLevel * (amplifier + 1);

        double verticalMultiplier = Math.max(0.10D, 1.0D - totalReduction);
        double horizontalMultiplier = Math.max(0.10D, 1.0D - totalReduction);

        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(
                motion.x * horizontalMultiplier,
                motion.y * verticalMultiplier,
                motion.z * horizontalMultiplier
        );

        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }
}