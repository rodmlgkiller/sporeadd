package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class gluttonousCrosshairRenderer {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        PlayerRenderer defaultRenderer = event.getSkin("default");
        if (defaultRenderer != null) {
            defaultRenderer.addLayer(new gluttonousCrosshairLayer(defaultRenderer));
        }

        PlayerRenderer slimRenderer = event.getSkin("slim");
        if (slimRenderer != null) {
            slimRenderer.addLayer(new gluttonousCrosshairLayer(slimRenderer));
        }
    }
}

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
class gluttonousCrosshairEvents {

    private static final ResourceLocation CROSSHAIR_TEXTURE =
            new ResourceLocation("sporeadd", "textures/entity/kommandant/crosshairbile.png");

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
    public static void onRenderCrosshair(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !gluttonousCrosshairLayer.isValidgluttonous(player)) return;

        event.setCanceled(true);

        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

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