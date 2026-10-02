package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.Powers.Poder12Variants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class GluttonousHelmetBurstJumpHandler {

    private static final int BURST_JUMP_EQUIVALENT_AMPLIFIER = 10;
    private static final int MAX_EFFECTIVE_AMPLIFIER = 15;

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (!Poder12Variants.isgluttonousHelmetBurstActive(player)) {
            return;
        }

        int amplifier = Math.min(BURST_JUMP_EQUIVALENT_AMPLIFIER, MAX_EFFECTIVE_AMPLIFIER);

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