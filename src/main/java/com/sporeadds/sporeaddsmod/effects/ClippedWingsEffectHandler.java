package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
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