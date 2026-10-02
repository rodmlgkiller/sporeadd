package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.layer.KommandantBlockLayer;
import com.sporeadds.sporeaddsmod.client.layer.KommandantEyeLayer;
import com.sporeadds.sporeaddsmod.client.layer.KommandantSpriteLayer;
import com.sporeadds.sporeaddsmod.client.renderer.layer.ChainArmLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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