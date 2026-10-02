package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ActionWheelOption {

    @FunctionalInterface
    public interface IconRenderer {
        void render(GuiGraphics graphics, int x, int y, int size);
    }

    private final Component label;
    private final IconRenderer icon;
    private final Runnable onSelect;
    @Nullable
    private final KeyMapping keyHint;

    public ActionWheelOption(Component label, IconRenderer icon, Runnable onSelect) {
        this(label, icon, onSelect, null);
    }

    public ActionWheelOption(Component label, IconRenderer icon, Runnable onSelect, @Nullable KeyMapping keyHint) {
        this.label = label;
        this.icon = icon;
        this.onSelect = onSelect;
        this.keyHint = keyHint;
    }

    public Component getLabel() {
        return label;
    }

    public IconRenderer getIcon() {
        return icon;
    }

    /** Atajo de teclado asociado a esta opción, para mostrarlo en la rueda. */
    @Nullable
    public KeyMapping getKeyHint() {
        return keyHint;
    }

    public void select() {
        onSelect.run();
    }
}
