package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Barra lateral de "Brutality" (Claws of Brutality). Se muestra a los jugadores berserker
 * cuando el contador tiene valor o las garras están activas.
 */
@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class BerserkerHudOverlay {

    private static final int BAR_X = 6;
    private static final int BAR_W = 8;
    private static final int BAR_H = 110;

    private static final int BORDER = 0xFF202020;
    private static final int BACK = 0xB0000000;
    private static final int FILL_BUILDING = 0xFFFFAA00;
    private static final int FILL_READY = 0xFFFFE23A;
    private static final int FILL_ACTIVE = 0xFFE23A2A;

    private BerserkerHudOverlay() {
    }

    private static boolean isBerserker() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(d -> "berserker".equalsIgnoreCase(d.getIdentifier()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!isBerserker() || !ClawCounterClientState.shouldShow()) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();

        int screenHeight = event.getWindow().getGuiScaledHeight();
        int x0 = BAR_X;
        int y0 = (screenHeight - BAR_H) / 2;
        int x1 = x0 + BAR_W;
        int y1 = y0 + BAR_H;

        float value = ClawCounterClientState.getValue();
        boolean active = ClawCounterClientState.isActive();
        float frac = Math.max(0.0F, Math.min(1.0F, value / ClawCounterClientState.MAX));
        int filled = Math.round(BAR_H * frac);

        int fillColor = active ? FILL_ACTIVE : (value >= ClawCounterClientState.MAX ? FILL_READY : FILL_BUILDING);

        graphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, BORDER);
        graphics.fill(x0, y0, x1, y1, BACK);
        if (filled > 0) {
            graphics.fill(x0, y1 - filled, x1, y1, fillColor);
        }

        // Syringes equipadas: sólo el icono, de abajo a arriba, junto a la barra.
        int iconX = x1 + 3;
        int drawn = 0;
        for (int i = 0; i < CompoundsClientState.SIZE; i++) {
            ItemStack s = CompoundsClientState.get(i);
            if (s.isEmpty()) continue;
            int iy = y1 - 16 - drawn * 18;
            graphics.renderItem(s, iconX, iy);
            drawn++;
        }
        int textX = iconX + 18;

        String num = String.valueOf((int) value);
        graphics.drawString(mc.font, num, textX, y0 - 1, fillColor, true);

        Component label = active
                ? Component.translatable("ability.sporeadds.berserker.claws")
                : Component.translatable("ability.sporeadds.berserker.claw_counter", (int) value);
        graphics.drawString(mc.font, label, textX, y1 - mc.font.lineHeight + 1, fillColor, true);
    }
}
