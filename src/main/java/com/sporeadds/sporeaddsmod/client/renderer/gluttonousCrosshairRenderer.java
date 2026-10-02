package com.sporeadds.sporeaddsmod.client.renderer;

import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class gluttonousCrosshairRenderer {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        PlayerRenderer defaultRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.WIDE);
        if (defaultRenderer != null) {
            defaultRenderer.addLayer(new gluttonousCrosshairLayer(defaultRenderer));
        }

        PlayerRenderer slimRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.SLIM);
        if (slimRenderer != null) {
            slimRenderer.addLayer(new gluttonousCrosshairLayer(slimRenderer));
        }
    }
}

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
class gluttonousCrosshairEvents {

    private static final ResourceLocation CROSSHAIR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/crosshairbile.png");

    private static final int SPRITE_SIZE = 32;
    private static final int TOTAL_SPRITES = 12;
    private static final int TEXTURE_HEIGHT = SPRITE_SIZE * TOTAL_SPRITES;

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!gluttonousCrosshairLayer.isValidgluttonous(mc.player)) return;

        if (ClientgluttonousCrosshairRenderState.isAttackOnCooldown(mc.player.getId())) {
            event.setCanceled(true);
            return;
        }

        if (mc.crosshairPickEntity != null) {
            ClientgluttonousCrosshairRenderState.markAttack(mc.player.getId());
        }
    }

    @SubscribeEvent
    public static void onRenderCrosshair(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !gluttonousCrosshairLayer.isValidgluttonous(player)) return;

        event.setCanceled(true);

        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int x = (screenWidth - SPRITE_SIZE) / 2;
        int y = (screenHeight - SPRITE_SIZE) / 2;

        int frameIndex = gluttonousCrosshairLayer.resolveFrame(player);

        RenderSystem.enableBlend();
        graphics.blit(
                CROSSHAIR_TEXTURE,
                x, y,
                0, frameIndex * SPRITE_SIZE,
                SPRITE_SIZE, SPRITE_SIZE,
                SPRITE_SIZE, TEXTURE_HEIGHT
        );
        RenderSystem.disableBlend();
    }
}