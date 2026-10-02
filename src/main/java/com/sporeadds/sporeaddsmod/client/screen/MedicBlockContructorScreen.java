package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.StartCraftingPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class MedicBlockContructorScreen extends AbstractContainerScreen<MedicBlockContructorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/medicblockcons.png");

    private Button Start;

    public MedicBlockContructorScreen(MedicBlockContructorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 126;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.leftPos + this.imageWidth / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonY = this.topPos + 20;


        this.Start = addRenderableWidget(Button.builder(
                Component.literal("START"),
                btn -> {
                    if (menu.getBlockEntity() != null) {
                        NetworkHandle.INSTANCE.sendToServer(
                                new StartCraftingPacket(menu.getBlockEntity().getBlockPos())
                        );
                    }
                }
        ).bounds(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)  {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Mostrar contador si está en crafting
        int ticks = menu.getCooldown();
        if (ticks > 0) {
            int minutes = ticks / 1200;
            int seconds = (ticks / 20) % 60;
            String text = String.format("%02d:%02d", minutes, seconds);

            int textWidth = this.font.width(text);
            int textX = this.leftPos + this.imageWidth / 2 - textWidth / 2;
            int textY = this.topPos + 5; // Ajusta altura del texto sobre el botón

            guiGraphics.drawString(this.font, text, textX, textY, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
    }
}
