package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.client.gui.MoundTerrariumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MoundTerrariumScreen extends AbstractContainerScreen<MoundTerrariumMenu> {

    // Ruta de la textura
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/mound_terrarium.png");

    public MoundTerrariumScreen(MoundTerrariumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // Tamaños según tu imagen (hasta el pixel 175,178)
        this.imageWidth = 176;
        this.imageHeight = 179;

        // Desactivamos el texto por defecto del inventario del jugador si no cabe bien
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Centramos la interfaz en la pantalla
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Dibuja el fondo principal de la interfaz
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 2. STOMACH (De arriba a abajo)
        // Tomado de: 1, 219 a 17, 254 (Ancho 16, Alto máximo 35)
        // Dibujado en: 14, 42
        int stomach = this.menu.getStomach();
        if (stomach > 0) {
            // Regla de 3 para escalar el valor (máx 36) a la altura visual (máx 35)
            int scaledStomach = (int) Math.round((stomach / 36.0) * 35.0);
            if (scaledStomach > 0) {
                guiGraphics.blit(TEXTURE, x + 14, y + 42, 1, 219, 16, scaledStomach);
            }
        }

        // 3. HP (De abajo a arriba, max 15)
        // Mismo tamaño visual pero movido 1 pixel derecha (+25 en vez de +24) y 1 pixel abajo (+71 en vez de +70)
        int hp = this.menu.getHp();
        if (hp > 0) {
            int offset = 15 - hp;
            guiGraphics.blit(TEXTURE, x + 25, y + 71 + offset, 115, 210 + offset, 15, hp);
        }

        // 4. SCENT (De abajo a arriba, max 25)
        int scent = this.menu.getScent();
        if (scent > 0) {
            int offset = 25 - scent;
            guiGraphics.blit(TEXTURE, x + 80, y + 46 + offset, 208, 131 + offset, 15, scent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY); // Renderiza los tooltips de los items
    }
}