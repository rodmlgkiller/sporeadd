package com.sporeadds.sporeaddsmod.effects;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class ClippedWingsEffectHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!player.hasEffect(effects.CLIPPED_WINGS.get())) {
            return;
        }

        if (player.isFallFlying()) {
            player.stopFallFlying();
            player.hasImpulse = true;
            player.hurtMarked = true;
        }
    }
}