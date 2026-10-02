package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.SporeAddsMod;
import com.sporeadds.sporeaddsmod.client.layer.KommandantBlockLayer;
import com.sporeadds.sporeaddsmod.client.layer.KommandantEyeLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (net.minecraft.client.resources.PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
            }
        }
    }
}