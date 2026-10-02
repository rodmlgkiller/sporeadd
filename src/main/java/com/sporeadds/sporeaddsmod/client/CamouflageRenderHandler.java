package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.renderer.GhostCamouflageLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
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