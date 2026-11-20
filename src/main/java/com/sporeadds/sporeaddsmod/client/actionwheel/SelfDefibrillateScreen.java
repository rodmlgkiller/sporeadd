package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sporeadds.sporeaddsmod.config.MedicConfig;
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

    private static final int FORCED_GUI_SCALE = 2;

    // Minigame rules. The server sends them when it opens the screen (medic.self_defibrillate.minigame.*).
    private final int totalIcons;
    private final int iconSize;
    private final int durationTicks;
    private final boolean iconsMove;
    private final double iconSpeed;

    private final List<IconSlot> icons = new ArrayList<>();
    private int correctIndex;
    private int ticksRemaining;
    private boolean resolved = false;
    private int lastSecondPlayed = -1;

    private int originalGuiScale = -1;
    private boolean guiScaleOverridden = false;

    /** Uses the minigame settings of this client's own config file (normally the server sends its own instead). */
    public SelfDefibrillateScreen() {
        this(MedicConfig.selfDefibrillateTimeLimitTicks(),
                MedicConfig.selfDefibrillateIconCount(),
                MedicConfig.selfDefibrillateIconSize(),
                MedicConfig.selfDefibrillateIconsMove(),
                MedicConfig.selfDefibrillateIconSpeed());
    }

    /**
     * @param durationTicks time to click the real icon, in ticks
     * @param totalIcons    icons on screen (1 real + fakes)
     * @param iconSize      icon side in GUI pixels
     * @param iconsMove     true = icons drift around and bounce off the screen edges
     * @param iconSpeed     speed of the drifting icons, in GUI pixels per tick
     */
    public SelfDefibrillateScreen(int durationTicks, int totalIcons, int iconSize, boolean iconsMove, float iconSpeed) {
        super(Component.literal("Self Defibrillate"));
        this.durationTicks = Math.max(1, durationTicks);
        this.totalIcons = Math.max(1, totalIcons);
        this.iconSize = Math.max(1, iconSize);
        this.iconsMove = iconsMove && iconSpeed > 0.0F;
        this.iconSpeed = iconSpeed;
        this.ticksRemaining = this.durationTicks;
    }

    /** One icon: its position (GUI pixels, top left corner) and, when icons move, its velocity per tick. */
    private static final class IconSlot {
        double x;
        double y;
        double prevX;
        double prevY;
        double vx;
        double vy;

        IconSlot(double x, double y) {
            this.x = x;
            this.y = y;
            this.prevX = x;
            this.prevY = y;
        }
    }

    @Override
    protected void init() {
        icons.clear();
        resolved = false;
        ticksRemaining = durationTicks;
        lastSecondPlayed = -1;

        forceGuiScale();

        Random random = new Random();
        int attempts = 0;
        int maxAttempts = 20000;

        while (icons.size() < totalIcons && attempts < maxAttempts) {
            attempts++;

            int x = random.nextInt(Math.max(1, this.width - iconSize));
            int y = random.nextInt(Math.max(1, this.height - iconSize));

            boolean overlaps = false;
            for (IconSlot existing : icons) {
                if (Math.abs(existing.x - x) < iconSize && Math.abs(existing.y - y) < iconSize) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                icons.add(new IconSlot(x, y));
            }
        }

        if (iconsMove) {
            for (IconSlot slot : icons) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                slot.vx = Math.cos(angle) * iconSpeed;
                slot.vy = Math.sin(angle) * iconSpeed;
            }
        }

        correctIndex = icons.isEmpty() ? -1 : new Random().nextInt(icons.size());

        AmbientSoundDamper.beginDuck();
    }

    /** Moves every icon one tick and bounces it off the screen edges (only called when icons move). */
    private void moveIcons() {
        double maxX = Math.max(0, this.width - iconSize);
        double maxY = Math.max(0, this.height - iconSize);

        for (IconSlot slot : icons) {
            slot.prevX = slot.x;
            slot.prevY = slot.y;

            slot.x += slot.vx;
            slot.y += slot.vy;

            if (slot.x < 0.0D) {
                slot.x = -slot.x;
                slot.vx = -slot.vx;
            } else if (slot.x > maxX) {
                slot.x = 2.0D * maxX - slot.x;
                slot.vx = -slot.vx;
            }

            if (slot.y < 0.0D) {
                slot.y = -slot.y;
                slot.vy = -slot.vy;
            } else if (slot.y > maxY) {
                slot.y = 2.0D * maxY - slot.y;
                slot.vy = -slot.vy;
            }

            slot.x = Math.max(0.0D, Math.min(maxX, slot.x));
            slot.y = Math.max(0.0D, Math.min(maxY, slot.y));
        }
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

        if (iconsMove) {
            moveIcons();
        }

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

        // Last to first: the icon drawn on top gets the click when moving icons overlap.
        for (int i = icons.size() - 1; i >= 0; i--) {
            IconSlot slot = icons.get(i);

            if (mouseX >= slot.x && mouseX <= slot.x + iconSize
                    && mouseY >= slot.y && mouseY <= slot.y + iconSize) {

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

            double drawX = slot.prevX + (slot.x - slot.prevX) * partialTick;
            double drawY = slot.prevY + (slot.y - slot.prevY) * partialTick;

            RenderSystem.setShaderTexture(0, texture);
            graphics.blit(
                    texture,
                    (int) Math.round(drawX), (int) Math.round(drawY),
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize
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