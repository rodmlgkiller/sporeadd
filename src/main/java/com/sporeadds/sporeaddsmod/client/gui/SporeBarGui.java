package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.config.SporeAddsClientConfig;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

public class SporeBarGui {
    private static final ResourceLocation SPORE_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/spore_bar.png");

    public static final IGuiOverlay SPORE_BAR = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!SporeClassUtil.hasClass(mc.player, "kommandant")) {
            return;
        }

        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        int spriteW = 202;
        int spriteH = 29;

        int x = (guiWidth - spriteW) / 2 + SporeAddsClientConfig.SPORE_BAR_X.get();
        int y = guiHeight - spriteH + SporeAddsClientConfig.SPORE_BAR_Y.get();

        float alpha = SporeAddsClientConfig.SPORE_BAR_OPACITY.get().floatValue();

        PlayerSporeProvider.PLAYER_CAP.get(mc.player).ifPresent(spore -> {
            int currentPhase = spore.getSpore();

            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, SPORE_BAR_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            guiGraphics.blit(SPORE_BAR_TEXTURE, x, y, 0, 29 * currentPhase, 202, 29, 2048, 2048);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();

            String sporeText = String.valueOf(currentPhase);

            float textScale = 0.85F;
            int mainColor = 0xFF8B0000;
            int outlineColor = 0xFF2B0000;

            float digitOffsetX = 0.0F;
            if (sporeText.length() == 1) {
                digitOffsetX = -0.75F;
            } else if (sporeText.length() == 2) {
                digitOffsetX = -0.25F;
            }

            float textCenterX = x + 109 + digitOffsetX;
            float textY = y - 7;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(textScale, textScale, 1.0F);

            float scaledX = (textCenterX - (mc.font.width(sporeText) / 2.0F)) / textScale;
            float scaledY = textY / textScale;

            guiGraphics.drawString(mc.font, sporeText, scaledX - 1.0F, scaledY, outlineColor, false);
            guiGraphics.drawString(mc.font, sporeText, scaledX + 1.0F, scaledY, outlineColor, false);
            guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY - 1.0F, outlineColor, false);
            guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY + 1.0F, outlineColor, false);

            guiGraphics.drawString(mc.font, sporeText, scaledX, scaledY, mainColor, true);

            guiGraphics.pose().popPose();
        });
    };
}