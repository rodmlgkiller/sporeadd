package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.layer.BerserkerClawLayer;
import com.sporeadds.sporeaddsmod.client.renderer.GhostCamouflageLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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