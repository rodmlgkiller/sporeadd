package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.FORGE)
public class TeleportBlockEvents {

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        Entity entity = event.getEntity();

        if (entity instanceof LivingEntity living && living.hasEffect(effects.ENCHAINED.get())) {
            event.setCanceled(true);
        }
    }
}