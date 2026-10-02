package com.sporeadds.sporeaddsmod.combat;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = "sporeadd")
public final class WeakPointEffectExpireHandler {

    private WeakPointEffectExpireHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        Set<Integer> trackedIds = new HashSet<>(WeakPointManager.getTrackedIds());
        if (trackedIds.isEmpty()) {
            return;
        }

        for (Integer id : trackedIds) {
            LivingEntity entity = WeakPointManager.resolveEntity(id);

            if (entity == null || !entity.isAlive()) {
                continue;
            }

            if (!entity.hasEffect(com.sporeadds.sporeaddsmod.effects.effects.EXPOSED_WEAKNESS.get())) {
                WeakPointManager.remove(id);

                com.sporeadds.sporeaddsmod.network.NetworkHandle.INSTANCE.send(
                        com.sporeadds.sporeaddsmod.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                        new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(id, false, null)
                );
            }
        }
    }
}