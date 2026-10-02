package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.layer.KommandantBlockLayer;
import com.sporeadds.sporeaddsmod.client.layer.KommandantEyeLayer;
import com.sporeadds.sporeaddsmod.client.layer.KommandantSpriteLayer;
import com.sporeadds.sporeaddsmod.client.renderer.layer.ChainArmLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SporeAddsClientRendererEvents {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new KommandantEyeLayer(renderer));
                renderer.addLayer(new KommandantBlockLayer(renderer));
                renderer.addLayer(new KommandantSpriteLayer(renderer));
                renderer.addLayer(new ChainArmLayer(renderer));
            }
        }
    }
}