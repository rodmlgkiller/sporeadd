package com.sporeadds.sporeaddsmod.Powers.bile;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class gluttonousKnockbackHandler {

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        int recentgluttonousHit = target.getPersistentData().getInt(gluttonousDamageHandler.RECENT_gluttonous_HIT_TAG);

        if (recentgluttonousHit > 0) {
            boolean wasBoneHit = target.getPersistentData().getBoolean(gluttonousDamageHandler.RECENT_gluttonous_BONE_HIT_TAG);

            if (!wasBoneHit) {
                event.setCanceled(true);
                target.setDeltaMovement(0.0D, target.getDeltaMovement().y, 0.0D);
                target.hurtMarked = true;
            }

            target.getPersistentData().remove(gluttonousDamageHandler.RECENT_gluttonous_HIT_TAG);
            target.getPersistentData().remove(gluttonousDamageHandler.RECENT_gluttonous_BONE_HIT_TAG);
        }
    }
}