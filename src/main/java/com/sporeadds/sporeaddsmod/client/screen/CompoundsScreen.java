package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.Powers.berserker.ClawsAbility;
import com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Pantalla del inventario de Compounds. Usa la textura {@code textures/gui/berserker.png}:
 * el archivo es de 352x332, pero el sprite de la GUI se toma solo de la esquina superior
 * izquierda, 176x166, a escala 1:1.
 */
public class CompoundsScreen extends AbstractContainerScreen<CompoundsMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("sporeadd", "textures/gui/berserker.png");

    private static final int TEX_W = 352;
    private static final int TEX_H = 332;

    // "Pantalla" de texto: de (17,12) a (158,29) en coordenadas de GUI.
    private static final int SCREEN_X = 17;
    private static final int SCREEN_Y = 12;
    private static final int SCREEN_W = 158 - 17;
    private static final int SCREEN_H = 29 - 12;
    private static final float TEXT_SCALE = 0.5F;

    private static final int GREEN = 0xFF55FF55;   // verde brillante
    private static final int RED = 0xFFFF5555;

    public CompoundsScreen(CompoundsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // El sprite se toma solo de (0,0) a (176,166), aunque el archivo sea 352x332.
        guiGraphics.blit(TEXTURE, x, y, this.imageWidth, this.imageHeight,
                0.0F, 0.0F, this.imageWidth, this.imageHeight, TEX_W, TEX_H);

        renderSummaryScreen(guiGraphics, x + SCREEN_X, y + SCREEN_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Sin etiquetas: la textura ya trae el arte.
    }

    private void renderSummaryScreen(GuiGraphics guiGraphics, int boxX, int boxY) {
        EnumMap<CompoundType, Integer> c = new EnumMap<>(CompoundType.class);
        for (int i = 0; i < CompoundsMenu.SLOT_COUNT; i++) {
            CompoundType t = CompoundType.fromItem(menu.slots.get(i).getItem().getItem());
            if (t != null) c.merge(t, 1, Integer::sum);
        }

        List<String> benefits = new ArrayList<>();
        int reinforced = c.getOrDefault(CompoundType.REINFORCED, 0);
        if (reinforced > 0) benefits.add(pct(-7.5 * reinforced) + " dmg taken");
        int skeletal = c.getOrDefault(CompoundType.SKELETAL, 0);
        if (skeletal > 0) {
            benefits.add(pct(7.5 * skeletal) + " speed");
            benefits.add(pct(25.0 * skeletal) + " KB resist");
        }
        int drowned = c.getOrDefault(CompoundType.DROWNED, 0);
        if (drowned > 0) {
            benefits.add("Water Breathing");
            benefits.add(pct(10.0 * drowned) + " swim");
        }
        int vampiric = c.getOrDefault(CompoundType.VAMPIRIC, 0);
        if (vampiric > 0) benefits.add("+" + vampiric + " HP/hit");
        int charred = c.getOrDefault(CompoundType.CHARRED, 0);
        if (charred > 0) {
            benefits.add("Fire Resistance");
            benefits.add("+" + charred + "s fire/hit");
        }
        int calcified = c.getOrDefault(CompoundType.CALCIFIED, 0);
        if (calcified > 0) benefits.add(pct(35.0 * calcified) + " knockback");
        int bezerk = c.getOrDefault(CompoundType.BEZERK, 0);
        if (bezerk > 0) benefits.add("+" + (2 * bezerk) + " unarmed dmg");
        int toxic = c.getOrDefault(CompoundType.TOXIC, 0);
        if (toxic > 0) benefits.add("Poison " + roman(toxic) + " " + (10 * toxic) + "s");
        int rotten = c.getOrDefault(CompoundType.ROTTEN, 0);
        if (rotten > 0) benefits.add("Wither " + roman(rotten) + " " + (10 * rotten) + "s");

        float baseHit = ClawsAbility.BARE_HAND_DAMAGE + 2.0F * bezerk;

        String greenText = (benefits.isEmpty() ? "No compounds equipped" : String.join("  ", benefits))
                + "  |  Base hit: " + trim(baseHit);
        String redText = "Brutality drain: " + (2 + bezerk) + "/s";

        int maxScaledWidth = Math.round(SCREEN_W / TEXT_SCALE);
        List<FormattedCharSequence> greenLines =
                this.font.split(Component.literal(greenText), maxScaledWidth);

        guiGraphics.enableScissor(boxX, boxY, boxX + SCREEN_W, boxY + SCREEN_H);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(boxX, boxY, 0.0D);
        pose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);

        int yy = 0;
        for (FormattedCharSequence line : greenLines) {
            guiGraphics.drawString(this.font, line, 0, yy, GREEN, false);
            yy += this.font.lineHeight;
        }
        guiGraphics.drawString(this.font, redText, 0, yy, RED, false);

        pose.popPose();
        guiGraphics.disableScissor();
    }

    private static String pct(double v) {
        String sign = v > 0 ? "+" : "";
        return sign + trim((float) v) + "%";
    }

    private static String trim(float v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.valueOf(Math.round(v * 10.0F) / 10.0F);
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            default -> String.valueOf(n);
        };
    }
}
