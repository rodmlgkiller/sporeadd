package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.commands.VervaTransportTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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