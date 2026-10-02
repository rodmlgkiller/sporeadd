package com.sporeadds.sporeaddsmod.combat;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public final class WeakPointDeathHandler {

    private WeakPointDeathHandler() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        int id = entity.getId();

        if (!WeakPointManager.hasWeakPoint(id)) {
            return;
        }

        WeakPointManager.remove(id);

        com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                net.neoforged.neoforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(id, false, null)
        );
    }
}