package com.sporeadds.sporeaddsmod.Powers.bile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
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