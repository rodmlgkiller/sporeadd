package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MedicBlockScreen extends AbstractContainerScreen<MedicBlockMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/medicblockscreen.png");

    public MedicBlockScreen(MedicBlockMenu pmenu, Inventory pinventory, Component pTitle) {
        super(pmenu, pinventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        inventoryLabelY = 10000;
        titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics pgraphics, float ppartialtick, int pmouseX, int pmouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) /2;
        int y = (height - imageHeight) /2;

        pgraphics.blit(TEXTURE, x, y, 0, 0, 176, 239, 256, 256);

        renderextras(pgraphics, x, y);
        renderProgressArrow(pgraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics pgraphics, int x, int y) {
        if (menu.isCrafting()) {
            pgraphics.blit(TEXTURE, x + 34, y + 70, 182, 155,menu.getScaledProgress(), 32,256,256);
            pgraphics.blit(TEXTURE, x + 34, y + 53, 184, 194,menu.getScaledProgress(), 10,256,256);
        }
    }

    private void renderextras(GuiGraphics pgraphics, int x, int y) {
        if (menu.isCrafting()) {
            pgraphics.blit(TEXTURE, x + 28, y + 36, 178, 206, 76, 30);
        }
        if (!menu.slots.get(0).getItem().isEmpty()) {
            pgraphics.blit(TEXTURE, x + 120, y + 48, 195, 52, 16, 92);
        }
        if (!menu.slots.get(1).getItem().isEmpty()) {
            pgraphics.blit(TEXTURE, x + 150, y + 68, 189, 16, 9, 29);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)  {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
