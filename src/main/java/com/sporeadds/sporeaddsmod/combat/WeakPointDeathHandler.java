package com.sporeadds.sporeaddsmod.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd")
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
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new com.sporeadds.sporeaddsmod.network.SyncWeakPointPacket(id, false, null)
        );
    }
}