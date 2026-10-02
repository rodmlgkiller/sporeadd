package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SubmitSelfDefibrillateChoicePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelfDefibrillateScreen extends Screen {

    private static final ResourceLocation FALSE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/defibrilator3.png");

    private static final ResourceLocation TRUE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/mob_effect/defibrilator.png");

    private static final ResourceLocation DETECTION_SOUND =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "detection");

    private static final int TOTAL_ICONS = 75;
    private static final int ICON_SIZE = 50;
    private static final int DURATION_TICKS = 60;

    private static final int FORCED_GUI_SCALE = 2;

    private final List<IconSlot> icons = new ArrayList<>();
    private int correctIndex;
    private int ticksRemaining = DURATION_TICKS;
    private boolean resolved = false;
    private int lastSecondPlayed = -1;

    private int originalGuiScale = -1;
    private boolean guiScaleOverridden = false;

    public SelfDefibrillateScreen() {
        super(Component.literal("Self Defibrillate"));
    }

    private record IconSlot(int x, int y) {
    }

    @Override
    protected void init() {
        icons.clear();
        resolved = false;
        ticksRemaining = DURATION_TICKS;
        lastSecondPlayed = -1;

        forceGuiScale();

        Random random = new Random();
        int attempts = 0;
        int maxAttempts = 20000;

        while (icons.size() < TOTAL_ICONS && attempts < maxAttempts) {
            attempts++;

            int x = random.nextInt(Math.max(1, this.width - ICON_SIZE));
            int y = random.nextInt(Math.max(1, this.height - ICON_SIZE));

            boolean overlaps = false;
            for (IconSlot existing : icons) {
                if (Math.abs(existing.x() - x) < ICON_SIZE && Math.abs(existing.y() - y) < ICON_SIZE) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                icons.add(new IconSlot(x, y));
            }
        }

        correctIndex = icons.isEmpty() ? -1 : new Random().nextInt(icons.size());

        AmbientSoundDamper.beginDuck();
    }

    private void forceGuiScale() {
        Minecraft mc = Minecraft.getInstance();
        int current = mc.options.guiScale().get();

        if (current == FORCED_GUI_SCALE) {
            guiScaleOverridden = false;
            return;
        }

        originalGuiScale = current;
        guiScaleOverridden = true;

        mc.options.guiScale().set(FORCED_GUI_SCALE);
        mc.resizeDisplay();
    }

    private void restoreGuiScale() {
        if (!guiScaleOverridden) return;

        Minecraft mc = Minecraft.getInstance();
        mc.options.guiScale().set(originalGuiScale);
        mc.resizeDisplay();

        guiScaleOverridden = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (resolved) return;

        ticksRemaining--;

        int secondsLeft = (int) Math.ceil(ticksRemaining / 20.0D);

        if (secondsLeft != lastSecondPlayed && secondsLeft >= 0) {
            lastSecondPlayed = secondsLeft;
            playDetectionSound();
        }

        if (ticksRemaining <= 0) {
            resolved = true;
            NetworkHandle.INSTANCE.sendToServer(new SubmitSelfDefibrillateChoicePacket(false));
            this.onClose();
        }
    }

    private void playDetectionSound() {
        net.minecraft.sounds.SoundEvent sound =
                BuiltInRegistries.SOUND_EVENT.get(DETECTION_SOUND);

        if (sound == null) return;

        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(sound, 1.0F, 1.0F)
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (resolved) return true;
        if (button != 0) return true;

        for (int i = 0; i < icons.size(); i++) {
            IconSlot slot = icons.get(i);

            if (mouseX >= slot.x() && mouseX <= slot.x() + ICON_SIZE
                    && mouseY >= slot.y() && mouseY <= slot.y() + ICON_SIZE) {

                resolved = true;
                boolean success = (i == correctIndex);

                NetworkHandle.INSTANCE.sendToServer(new SubmitSelfDefibrillateChoicePacket(success));
                this.onClose();
                return true;
            }
        }

        return true;
    }

    @Override
    public void onClose() {
        AmbientSoundDamper.endDuck();
        restoreGuiScale();
        super.onClose();
    }

    /** The vanilla blur would be drawn over everything these screens paint before super.render(). */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)  {
        this.renderTransparentBackground(graphics);

        for (int i = 0; i < icons.size(); i++) {
            IconSlot slot = icons.get(i);
            ResourceLocation texture = (i == correctIndex) ? TRUE_TEXTURE : FALSE_TEXTURE;

            RenderSystem.setShaderTexture(0, texture);
            graphics.blit(
                    texture,
                    slot.x(), slot.y(),
                    0, 0,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE, ICON_SIZE
            );
        }

        int secondsLeft = (int) Math.ceil(ticksRemaining / 20.0D);
        Component timerText = Component.literal(String.valueOf(Math.max(secondsLeft, 0)));

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(
                this.font,
                timerText,
                centerX,
                centerY,
                0xFFFF0000
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}