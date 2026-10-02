package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class VervaRenderEvents {

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity() != null) {
            if (VervaTransportTask.isPlayerTraveling(event.getEntity().getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onHandRender(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && VervaTransportTask.isPlayerTraveling(player.getUUID())) {
            event.setCanceled(true);
        }
    }
}