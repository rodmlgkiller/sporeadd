package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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
