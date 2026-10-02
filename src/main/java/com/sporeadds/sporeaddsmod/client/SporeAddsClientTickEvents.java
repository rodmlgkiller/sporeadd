package com.sporeadds.sporeaddsmod.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.renderer.ClientgluttonousCrosshairRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SporeAddsClientTickEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        {
            ClientgluttonousCrosshairRenderState.cleanup();
        }
    }
}