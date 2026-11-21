package com.sporeadds.sporeaddsmod.client.config;

import com.sporeadds.sporeaddsmod.config.SporeAddsClientConfig;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Mods -> SporeAdds -> Config. Forge 1.20.1 has no generic configuration screen, so this mod ships its own: a front page
 * that opens the gameplay config and the client (HUD) config, each browsable section by section.
 */
public class SporeConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 260;

    private final Screen parent;

    public SporeConfigScreen(Screen parent) {
        super(Component.literal("SporeAdds configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = this.height / 2 - 40;

        this.addRenderableWidget(Button.builder(Component.literal("Gameplay settings"),
                        b -> this.minecraft.setScreen(new ConfigSectionScreen(this, Component.literal("Gameplay settings"),
                                SporeAddsConfig.SPEC, SporeAddsConfig.SPEC.getValues(), List.of())))
                .bounds(x, y, BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Client settings (HUD)"),
                        b -> this.minecraft.setScreen(new ConfigSectionScreen(this, Component.literal("Client settings (HUD)"),
                                SporeAddsClientConfig.SPEC, SporeAddsClientConfig.SPEC.getValues(), List.of())))
                .bounds(x, y + 28, BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(x, y + 76, BUTTON_WIDTH, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        // The notice wraps on narrow windows instead of being cut at both edges.
        List<FormattedCharSequence> notice = this.font.split(
                Component.literal("Gameplay settings are applied by the world's host or server (sporeadds-config.toml)."),
                Math.min(this.width - 20, 380));
        int noticeTop = this.height / 2 - 46 - notice.size() * (this.font.lineHeight + 1);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, noticeTop - 16, 0xFFFFFF);
        for (int i = 0; i < notice.size(); i++) {
            graphics.drawCenteredString(this.font, notice.get(i), this.width / 2, noticeTop + i * (this.font.lineHeight + 1), 0xAAAAAA);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        ConfigUi.flush();
    }

    @Override
    public void onClose() {
        ConfigUi.flush();
        this.minecraft.setScreen(parent);
    }
}
