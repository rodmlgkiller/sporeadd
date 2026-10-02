package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.client.ClientAbilityChargeState;
import com.sporeadds.sporeaddsmod.client.ClientGluttonousFragmentsState;
import com.sporeadds.sporeaddsmod.config.SporeAddsClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

/**
 * Special-ability bar for the Caustic/Gluttonous Kommandant subclasses (X by default), replacing the old
 * action-bar text hacks. Caustic gets a continuous charge fill (border/back/fill, like the Berserker
 * brutality bar but horizontal); Gluttonous gets a row of discrete ammo-fragment segments, colored by
 * fragment type, since it fires stored shots of different ammo rather than charging up a single shot.
 * Sits above the spore bar by default; nudgeable via the client config.
 */
public class AbilityChargeBarGui {

    private static final int BAR_W = 120;
    private static final int BAR_H = 8;
    private static final int DEFAULT_Y_RAISE = 7;

    private static final int BORDER = 0xFF202020;
    private static final int BACK = 0xB0000000;

    // Caustic: hand-painted fill sprite, cropped horizontally by charge fraction (same technique as
    // the vanilla XP bar). Slight brighten tint once fully charged, no tint while still charging.
    private static final ResourceLocation CAUSTIC_FILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/caustic_bar_fill.png");
    private static final int CAUSTIC_FILL_TEX_W = 120;
    private static final int CAUSTIC_FILL_TEX_H = 8;

    // Gluttonous: one segment per stored ammo fragment. Segment sprites come from a shared 24x8 sheet
    // (only the first 15px are used), 5x8 each, left to right: gore, generic (gluttonous/other), bone.
    private static final int SEGMENT_GAP = 1;
    private static final ResourceLocation GLUTTONOUS_SEGMENTS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/gluttonous_bar_segments.png");
    private static final int GLUTTONOUS_SHEET_W = 24;
    private static final int GLUTTONOUS_SHEET_H = 8;
    private static final int GLUTTONOUS_SEGMENT_W = 5;
    private static final int GLUTTONOUS_U_GORE = 0;
    private static final int GLUTTONOUS_U_GENERIC = 5;
    private static final int GLUTTONOUS_U_BONE = 10;
    private static final int FRAGMENT_EMPTY = 0xFF555555;

    public static final IGuiOverlay ABILITY_CHARGE_BAR = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String subclass = getKommandantSubclass(mc);
        if (subclass == null) return;

        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();

        // Base sits directly above the spore bar (202x29 sprite), tracking its own configured offset
        // so the two stay stacked even if the spore bar is repositioned.
        int spawnBaseY = guiHeight - 29 + SporeAddsClientConfig.SPORE_BAR_Y.get();

        int x0 = (guiWidth - BAR_W) / 2 + SporeAddsClientConfig.ABILITY_CHARGE_BAR_X.get();
        int y0 = spawnBaseY - BAR_H - 4 - DEFAULT_Y_RAISE + SporeAddsClientConfig.ABILITY_CHARGE_BAR_Y.get();
        int x1 = x0 + BAR_W;
        int y1 = y0 + BAR_H;

        if (subclass.equals("caustic")) {
            if (!ClientAbilityChargeState.shouldShow()) return;
            renderCausticBar(guiGraphics, x0, y0, x1, y1);
        } else if (subclass.equals("gluttonous")) {
            if (!ClientGluttonousFragmentsState.shouldShow()) return;
            renderGluttonousBar(guiGraphics, x0, y0, x1, y1, ClientGluttonousFragmentsState.get());
        }
    };

    private static void renderCausticBar(net.minecraft.client.gui.GuiGraphics guiGraphics, int x0, int y0, int x1, int y1) {
        float frac = Math.max(0.0F, Math.min(1.0F,
                ClientAbilityChargeState.getTicks() / (float) ClientAbilityChargeState.MAX_TICKS));
        int filled = Math.round(BAR_W * frac);

        guiGraphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, BORDER);
        guiGraphics.fill(x0, y0, x1, y1, BACK);
        if (filled > 0) {
            boolean ready = ClientAbilityChargeState.isFullyCharged();
            if (ready) {
                RenderSystem.setShaderColor(1.25F, 1.25F, 1.25F, 1.0F);
            }
            guiGraphics.blit(CAUSTIC_FILL_TEXTURE, x0, y0, filled, BAR_H,
                    0.0F, 0.0F, filled, BAR_H, CAUSTIC_FILL_TEX_W, CAUSTIC_FILL_TEX_H);
            if (ready) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private static void renderGluttonousBar(net.minecraft.client.gui.GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, List<String> fragments) {
        int maxSlots = com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler.getMaxFragments();

        guiGraphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, BORDER);
        guiGraphics.fill(x0, y0, x1, y1, BACK);

        // Boundaries are computed per-slot from the full bar width (not a fixed integer slot width),
        // so the segments always span the whole bar with no leftover gap on the right from rounding.
        float barWidth = x1 - x0;
        for (int i = 0; i < maxSlots; i++) {
            int segStart = x0 + Math.round(i * barWidth / maxSlots);
            int segEnd = Math.min(x1, x0 + Math.round((i + 1) * barWidth / maxSlots) - SEGMENT_GAP);
            if (segEnd <= segStart) segEnd = Math.min(x1, segStart + 1);
            int segWidth = segEnd - segStart;

            if (i < fragments.size()) {
                int u = uOffsetFor(fragments.get(i));
                guiGraphics.blit(GLUTTONOUS_SEGMENTS_TEXTURE, segStart, y0, segWidth, BAR_H,
                        (float) u, 0.0F, GLUTTONOUS_SEGMENT_W, BAR_H, GLUTTONOUS_SHEET_W, GLUTTONOUS_SHEET_H);
            } else {
                guiGraphics.fill(segStart, y0, segEnd, y1, FRAGMENT_EMPTY);
            }
        }
    }

    private static int uOffsetFor(String fragmentType) {
        return switch (fragmentType) {
            case "bone" -> GLUTTONOUS_U_BONE;
            case "gore" -> GLUTTONOUS_U_GORE;
            default -> GLUTTONOUS_U_GENERIC;
        };
    }

    private static String getKommandantSubclass(Minecraft mc) {
        return mc.player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).map(data -> {
            if (!"kommandant".equals(data.getIdentifier())) return null;
            String sub = data.getSubclass();
            return ("caustic".equals(sub) || "gluttonous".equals(sub)) ? sub : null;
        }).orElse(null);
    }
}
