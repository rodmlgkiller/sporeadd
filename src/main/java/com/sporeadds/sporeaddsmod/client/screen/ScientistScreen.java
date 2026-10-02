package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.client.gui.ButtonArea;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.ResearchRequestPacket;
import com.sporeadds.sporeaddsmod.ModItems;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ScientistScreen extends AbstractContainerScreen<ScientistMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/scientist_gui.png");

    public ScientistScreen(ScientistMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        this.addRenderableWidget(new ButtonArea(this.leftPos + 8, this.topPos + 55, 16, 16,
                () -> {
            NetworkHandle.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                    new ResearchRequestPacket(menu.blockEntity.getBlockPos(), 50));
        }));

        this.addRenderableWidget(new ButtonArea(this.leftPos + 33, this.topPos + 55, 16, 16,
                () -> {
            NetworkHandle.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                    new ResearchRequestPacket(menu.blockEntity.getBlockPos(), 100));
        }));

        this.addRenderableWidget(new ButtonArea(this.leftPos + 58, this.topPos + 55, 16, 16,
                () -> {
            NetworkHandle.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                    new ResearchRequestPacket(menu.blockEntity.getBlockPos(), 150));
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, 176, 179);
        renderProgressArrow(guiGraphics, x ,y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        int arrowrender =  menu.getStoredBiomass() * 150 / 150;
        guiGraphics.blit(TEXTURE, x + 17, y + 6, 4, 185,arrowrender, 7,256,256);
    }

    private void renderResearchButtons(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.renderItem(new ItemStack(ModItems.RESEARCH_50.get()), x + 44, y + 53);
        guiGraphics.renderItem(new ItemStack(ModItems.RESEARCH_100.get()), x + 62, y + 53);
        guiGraphics.renderItem(new ItemStack(ModItems.RESEARCH_150.get()), x + 80, y + 53);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Renderizar número de biomasa en cian
        int biomass = menu.getStoredBiomass();
        guiGraphics.drawString(this.font, String.valueOf(biomass),
                leftPos + 46, topPos + 25, 0x00FFFF);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
