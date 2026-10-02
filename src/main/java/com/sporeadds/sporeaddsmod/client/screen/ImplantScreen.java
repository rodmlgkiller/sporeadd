package com.sporeadds.sporeaddsmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ImplantScreen extends AbstractContainerScreen<ImplantMenu> {

    // Clase interna para agrupar datos de GUI por ítem
    public static record ItemGuiData(ItemStack stack, int screenX, int screenY, int texU, int texV, int texWidth, int texHeight) {}

    // Lista de ítems con sus posiciones y tamaños en la textura
    private final List<ItemGuiData> itemsGui = List.of(
            new ItemGuiData(new ItemStack(ModItems.EYE_IMPLANT.get()),   46, 27, 200, 18, 22, 18),
            new ItemGuiData(new ItemStack(ModItems.TORSO_IMPLANT.get()), 43, 46, 197, 41, 28, 33),
            new ItemGuiData(new ItemStack(ModItems.RIGHTARM_IMPLANT.get()), 71, 46, 226, 41, 12, 30),
            new ItemGuiData(new ItemStack(ModItems.LEFTARM_IMPLANT.get()), 31, 46, 184, 41, 12, 30),
            new ItemGuiData(new ItemStack(ModItems.RIGHTLEG_IMPLANT.get()), 58, 80, 211, 77,15,31),
            new ItemGuiData(new ItemStack(ModItems.LEFTLEG_IMPLANT.get()), 42, 80, 196, 77, 15, 31)
    );

    private static final ResourceLocation TEXTURE = new ResourceLocation("sporeadd", "textures/gui/implantgui.png");

    public ImplantScreen(ImplantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 212; // Ajusta el tamaño de la GUI
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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

        // Dibujar el fondo de la GUI
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Dibujar cada ítem de la lista
        for (int i = 0; i < itemsGui.size(); i++) {
            ItemGuiData data = itemsGui.get(i);

            ItemStack stack = menu.slots.get(i).getItem();
            if (stack.is(data.stack.getItem())) {
                guiGraphics.blit(
                        TEXTURE,
                        x + data.screenX, y + data.screenY,   // posición en pantalla
                        data.texU, data.texV,                // posición en textura
                        data.texWidth, data.texHeight        // tamaño del rectángulo
                );
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
    }
}