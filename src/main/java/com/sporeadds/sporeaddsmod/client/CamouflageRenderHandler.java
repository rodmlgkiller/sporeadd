package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.client.renderer.GhostCamouflageLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class CamouflageRenderHandler {

    private static final Map<PlayerRenderer, GhostCamouflageLayer> LAYER_CACHE = new HashMap<>();

    private CamouflageRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        if (!CamouflageClientState.isCamouflaged(player.getId())) {
            return;
        }

        event.setCanceled(true);

        PlayerRenderer playerRenderer = event.getRenderer();

        GhostCamouflageLayer layer = LAYER_CACHE.computeIfAbsent(playerRenderer, GhostCamouflageLayer::new);

        layer.render(
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                player,
                0F, 0F,
                event.getPartialTick(),
                0F, 0F, 0F
        );
    }
}