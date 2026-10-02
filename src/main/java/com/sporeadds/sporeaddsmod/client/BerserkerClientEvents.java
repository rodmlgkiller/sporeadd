package com.sporeadds.sporeaddsmod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BerserkerClientEvents {

    private BerserkerClientEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        BerserkerClawRenderState.clear();
        ClawCounterClientState.set(0.0F, false);
        CompoundsClientState.clear();
        BerserkerCooldownClientState.setCooldown(BerserkerCooldownClientState.COUNTER, 0);
        BerserkerCooldownClientState.setCooldown(BerserkerCooldownClientState.CLAWS, 0);
    }
}
