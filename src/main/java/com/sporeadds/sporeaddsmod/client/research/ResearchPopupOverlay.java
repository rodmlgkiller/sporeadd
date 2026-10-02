package com.sporeadds.sporeaddsmod.client.research;

import com.sporeadds.sporeaddsmod.research.ResearchPopupManager;
import com.sporeadds.sporeaddsmod.research.ResearchPopupType;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "sporeadd", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ResearchPopupOverlay {

    private static final int POPUP_LINE_HEIGHT = 12;
    private static final int BOTTOM_MARGIN = 45;
    private static final int RIGHT_MARGIN = 8;
    private static final int DATA_COLOR_STEP = 20;

    private ResearchPopupOverlay() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().toString().equals("minecraft:hotbar")) {
            return;
        }

        ResearchPopupManager.tick();
        List<ResearchPopupManager.Popup> popups = ResearchPopupManager.getActivePopups();
        if (popups.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int rightX = mc.getWindow().getGuiScaledWidth() - RIGHT_MARGIN;
        int bottomY = mc.getWindow().getGuiScaledHeight() - BOTTOM_MARGIN;

        int y = bottomY - (popups.size() * POPUP_LINE_HEIGHT);
        for (ResearchPopupManager.Popup popup : popups) {
            Component line = buildComponent(popup);
            int width = mc.font.width(line);
            graphics.drawString(mc.font, line, rightX - width, y, 0xFFFFFF, true);
            y += POPUP_LINE_HEIGHT;
        }
    }

    private static Component buildComponent(ResearchPopupManager.Popup popup) {
        String displayName = resolveDisplayName(popup.entityId);
        int rarity = TrackedEntities.getRarity(popup.entityId);

        return switch (popup.type) {
            case UNLOCK -> Component.translatable(
                    "popup.sporeadds.entry_unlocked", displayName
            ).withStyle(rarityColor(rarity));

            case PRESTIGE -> Component.translatable(
                    "popup.sporeadds.prestige_obtained", displayName
            ).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

            case DATA -> Component.translatable(
                    "popup.sporeadds.data_obtained", "+" + popup.amount, displayName
            ).withStyle(dataColor(popup.amount));
        };
    }

    private static String resolveDisplayName(String entityId) {
        if (TrackedEntities.isKommandantPlayerEntry(entityId)) {
            return Component.translatable("gui.sporeadds.field_research.kommandant_player").getString();
        }
        return Component.translatable("entity." + entityId.replace(":", ".")).getString();
    }

    private static ChatFormatting rarityColor(int rarity) {
        return switch (rarity) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.GREEN;
            case 3 -> ChatFormatting.LIGHT_PURPLE;
            case 4 -> ChatFormatting.GOLD;
            default -> ChatFormatting.WHITE;
        };
    }

    private static ChatFormatting dataColor(int amount) {
        int tier = amount / DATA_COLOR_STEP;

        return switch (tier % 6) {
            case 0 -> ChatFormatting.AQUA;
            case 1 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.GOLD;
            case 3 -> ChatFormatting.RED;
            case 4 -> ChatFormatting.LIGHT_PURPLE;
            case 5 -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.AQUA;
        };
    }
}