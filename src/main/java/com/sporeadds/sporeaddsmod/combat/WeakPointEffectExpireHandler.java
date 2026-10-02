package com.sporeadds.sporeaddsmod.combat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class WeakPointEffectExpireHandler {

    private WeakPointEffectExpireHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

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
                        net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                        new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(id, false, null)
                );
            }
        }
    }
}