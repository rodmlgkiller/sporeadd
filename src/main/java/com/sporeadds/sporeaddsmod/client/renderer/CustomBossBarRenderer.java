package com.sporeadds.sporeaddsmod.client.renderer;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public class CustomBossBarRenderer {

    private static final ResourceLocation BOSS_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/bossbar.png");
    private static final ResourceLocation BOSS_BAR_ABYSSAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/bossbar_abyssal.png");
    private static final ResourceLocation BOSS_BAR_CAUSTIC_TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/bossbar_caustic.png");
    private static final ResourceLocation BOSS_BAR_GLUTTONOUS_TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/bossbar_gluttonous.png");

    private static final int FRAME_WIDTH = 193;
    private static final int FRAME_HEIGHT = 20;
    private static final int TOTAL_FRAMES = 42;

    @SubscribeEvent
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (event.getBossEvent().getColor() != BossEvent.BossBarColor.PINK ||
                event.getBossEvent().getOverlay() != BossEvent.BossBarOverlay.NOTCHED_20) {
            return;
        }

        event.setCanceled(true);

        GuiGraphics graphics = event.getGuiGraphics();
        float progress = event.getBossEvent().getProgress();

        String rawName = event.getBossEvent().getName().getString();
        String subclass = "default";
        String visibleName = rawName;

        if (rawName.startsWith("[sporeadd|")) {
            int end = rawName.indexOf(']');
            if (end > 10) {
                subclass = rawName.substring(10, end).trim().toLowerCase();
                visibleName = rawName.substring(end + 1);
            }
        }

        ResourceLocation texture = BOSS_BAR_TEXTURE;
        if ("abyssal".equals(subclass)) {
            texture = BOSS_BAR_ABYSSAL_TEXTURE;
        } else if ("caustic".equals(subclass)) {
            texture = BOSS_BAR_CAUSTIC_TEXTURE;
        } else if ("gluttonous".equals(subclass)) {
            texture = BOSS_BAR_GLUTTONOUS_TEXTURE;
        }

        int maxFrameIndex = TOTAL_FRAMES - 1;
        int frameIndex = Math.max(0, Math.min(maxFrameIndex, (int) ((1.0f - progress) * maxFrameIndex)));
        int vOffset = frameIndex * FRAME_HEIGHT;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenX = (screenWidth / 2) - (FRAME_WIDTH / 2);
        int screenY = event.getY();

        Component displayName = Component.literal(visibleName);
        int textWidth = Minecraft.getInstance().font.width(displayName);
        int textX = (screenWidth / 2) - (textWidth / 2);
        int textY = screenY - 9;

        graphics.drawString(Minecraft.getInstance().font, displayName, textX, textY, 0xAA0000, true);
        graphics.blit(texture, screenX, screenY, 0, vOffset, FRAME_WIDTH, FRAME_HEIGHT, 256, 1024);

        event.setIncrement(FRAME_HEIGHT + 14);
    }
}