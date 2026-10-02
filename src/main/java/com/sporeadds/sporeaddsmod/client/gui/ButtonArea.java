package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ButtonArea extends AbstractWidget {
    private final Runnable onClick;

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.run();
    }

    public ButtonArea(int x, int y, int width, int height, Runnable onClick){
        super(x,y,width,height, Component.empty());
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY){
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
    }
}
