package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KommandantSubclassIconRenderer {

    private static final ResourceLocation DISSOLUTION_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/dissolution.png");

    private static final ResourceLocation EXPOSED_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/exposed.png");

    private static final ResourceLocation ABYSSAL_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/abyssal.png");

    private static final ResourceLocation GLUTTONOUS_ICON =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/gluttonous.png");

    private static final int ICON_SIZE = 18;

    public static void render(GuiGraphics guiGraphics, int x, int y, float scale) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) return;

            ResourceLocation texture = getTextureForSubclass(data.getSubclass());
            if (texture == null) return;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.blit(texture, 0, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            guiGraphics.pose().popPose();
        });
    }

    public static void renderTooltip(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float scale) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!"kommandant".equalsIgnoreCase(data.getIdentifier())) return;

            String subclass = data.getSubclass();
            ResourceLocation texture = getTextureForSubclass(subclass);
            if (texture == null) return;

            int scaledSize = Math.max(1, Math.round(ICON_SIZE * scale));
            if (isMouseOver(mouseX, mouseY, x, y, scaledSize, scaledSize)) {
                List<Component> tooltip = getSubclassDescription(subclass);
                if (!tooltip.isEmpty()) {
                    guiGraphics.renderTooltip(mc.font, tooltip, Optional.<TooltipComponent>empty(), mouseX, mouseY);
                }
            }
        });
    }

    private static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static List<Component> getSubclassDescription(String subclass) {
        List<Component> tooltip = new ArrayList<>();
        if (subclass == null) return tooltip;

        switch (subclass.toLowerCase()) {
            case "caustic" -> {
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.caustic.title"));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.caustic.desc"));
            }
            case "abyssal" -> {
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.abyssal.title"));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.abyssal.desc"));
            }
            case "gluttonous" -> {
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.gluttonous.title"));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.gluttonous.desc"));
            }
            case "none" -> {
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.none.title"));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.sporeadd.kommandant_subclass.none.desc"));
            }
        }

        return tooltip;
    }

    private static ResourceLocation getTextureForSubclass(String subclass) {
        if (subclass == null) return null;

        return switch (subclass.toLowerCase()) {
            case "caustic" -> DISSOLUTION_ICON;
            case "abyssal" -> ABYSSAL_ICON;
            case "gluttonous" -> GLUTTONOUS_ICON;
            case "none" -> EXPOSED_ICON;
            default -> null;
        };
    }
}