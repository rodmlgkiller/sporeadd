package com.sporeadds.sporeaddsmod.client.actionwheel;

import com.sporeadds.sporeaddsmod.client.SporeKeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ActionWheelScreen extends Screen {

    private static final int RADIUS = 70;
    private static final int ICON_SIZE = 20;
    private static final double MIN_SELECT_DISTANCE = 12.0D;

    private final List<ActionWheelOption> options;
    private int hoveredIndex = -1;
    private boolean closed = false;

    protected ActionWheelScreen(List<ActionWheelOption> options) {
        super(Component.empty());
        this.options = options;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.fill(0, 0, this.width, this.height, 0x66000000);

        this.hoveredIndex = computeHoveredIndex(centerX, centerY, mouseX, mouseY);

        int count = options.size();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i / count) - (Math.PI / 2);

            boolean hovered = i == hoveredIndex;
            int size = hovered ? ICON_SIZE + 6 : ICON_SIZE;

            int drawX = centerX + (int) (Math.cos(angle) * RADIUS) - size / 2;
            int drawY = centerY + (int) (Math.sin(angle) * RADIUS) - size / 2;

            if (hovered) {
                graphics.fill(drawX - 3, drawY - 3, drawX + size + 3, drawY + size + 3, 0x80FFFFFF);
            }

            options.get(i).getIcon().render(graphics, drawX, drawY, size);

            net.minecraft.client.KeyMapping keyHint = options.get(i).getKeyHint();
            if (keyHint != null) {
                Component keyText = Component.literal("[").append(keyHint.getTranslatedKeyMessage()).append("]");
                int kw = this.font.width(keyText);
                graphics.drawString(this.font, keyText,
                        drawX + size / 2 - kw / 2, drawY + size + 2, 0xFFAAAAAA);
            }
        }

        if (hoveredIndex >= 0) {
            Component label = options.get(hoveredIndex).getLabel();
            int textWidth = this.font.width(label);
            graphics.drawString(this.font, label, centerX - textWidth / 2, centerY - 4, 0xFFFFFF);
        }
    }

    private int computeHoveredIndex(int centerX, int centerY, int mouseX, int mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < MIN_SELECT_DISTANCE) {
            return -1;
        }

        double angle = Math.atan2(dy, dx) + (Math.PI / 2);
        if (angle < 0) {
            angle += Math.PI * 2;
        }

        int count = options.size();
        double slice = Math.PI * 2 / count;
        return (int) Math.round(angle / slice) % count;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!closed && SporeKeyMapping.OPEN_ACTION_WHEEL.matches(keyCode, scanCode)) {
            confirmSelection();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            confirmSelection();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void confirmSelection() {
        if (closed) {
            return;
        }
        closed = true;

        if (hoveredIndex >= 0 && hoveredIndex < options.size()) {
            options.get(hoveredIndex).select();
        }

        ActionWheelHandler.notifyClosedExternally();

        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}