package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class VervaCountdownOverlay {

    public static final net.minecraft.client.gui.LayeredDraw.Layer OVERLAY = (guiGraphics, deltaTracker) -> {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientVervaCountdownData.CountdownEntry entry = ClientVervaCountdownData.getHighestPriority();
        if (entry == null) return;

        String text = String.valueOf(entry.seconds);

        float textScale = 2.0F;
        int mainColor = 0xFF8B0000;
        int outlineColor = 0xFF2B0000;

        float centerX = screenWidth - 40.0F;
        float centerY = (screenHeight / 2.0F) - 10.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, 1.0F);

        float scaledX = (centerX - (mc.font.width(text) / 2.0F)) / textScale;
        float scaledY = centerY / textScale;

        guiGraphics.drawString(mc.font, text, scaledX - 1.0F, scaledY, outlineColor, false);
        guiGraphics.drawString(mc.font, text, scaledX + 1.0F, scaledY, outlineColor, false);
        guiGraphics.drawString(mc.font, text, scaledX, scaledY - 1.0F, outlineColor, false);
        guiGraphics.drawString(mc.font, text, scaledX, scaledY + 1.0F, outlineColor, false);

        guiGraphics.drawString(mc.font, text, scaledX, scaledY, mainColor, true);

        guiGraphics.pose().popPose();
    };
}