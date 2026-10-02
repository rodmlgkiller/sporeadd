package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.renderer.ClientgluttonousCrosshairRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SporeAddsClientTickEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientgluttonousCrosshairRenderState.cleanup();
        }
    }
}