package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class PostmortemJumpHandler {

    private static final double JUMP_REDUCTION = 0.50D;

    private PostmortemJumpHandler() {
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        MobEffectInstance postmortem = entity.getEffect(effects.POSTMORTEM.get());
        if (postmortem == null) return;

        double verticalMultiplier = Math.max(0.10D, 1.0D - JUMP_REDUCTION);

        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(
                motion.x,
                motion.y * verticalMultiplier,
                motion.z
        );

        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }
}