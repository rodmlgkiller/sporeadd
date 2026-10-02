package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class TeleportBlockEvents {

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        Entity entity = event.getEntity();

        if (entity instanceof LivingEntity living && living.hasEffect(effects.ENCHAINED)) {
            event.setCanceled(true);
        }
    }
}