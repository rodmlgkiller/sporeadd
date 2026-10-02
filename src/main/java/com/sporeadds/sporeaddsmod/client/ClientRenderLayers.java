package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.layer.BerserkerClawLayer;
import com.sporeadds.sporeaddsmod.client.renderer.GhostCamouflageLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientRenderLayers {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, ?> renderer = event.getSkin(skin);

            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new GhostCamouflageLayer(playerRenderer));
                playerRenderer.addLayer(new BerserkerClawLayer(playerRenderer));
            }
        }
    }
}