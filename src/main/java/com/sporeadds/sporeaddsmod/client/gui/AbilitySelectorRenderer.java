package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class AbilitySelectorRenderer {

    public static void renderSporeValue(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int spore) {
        String sporeText = String.valueOf(spore);

        float textScale = 2.3F;

        int mainColor = 0xFF8B0000;
        int outlineColor = 0xFF2B0000;

        int digitOffsetX = 0;
        if (sporeText.length() == 1) {
            digitOffsetX = -15;
        } else if (sporeText.length() == 2) {
            digitOffsetX = -10;
        }

        int baseX = x + 38 * 2 + digitOffsetX;
        int baseY = y + 174 * 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, 1.0F);

        int scaledTextWidth = mc.font.width(sporeText);
        int scaledX = (int) ((baseX - (scaledTextWidth / 2)) / textScale);
        int scaledY = (int) (baseY / textScale);

        guiGraphics.drawString(mc.font, sporeText, scaledX - 1, scaledY, outlineColor, false);
        guiGraphics.drawString(mc.font, sporeText, scaledX + 1, scaledY, outlineColor, false);
        guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY - 1, outlineColor, false);
        guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY + 1, outlineColor, false);

        guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY, mainColor, true);

        guiGraphics.pose().popPose();
    }
}