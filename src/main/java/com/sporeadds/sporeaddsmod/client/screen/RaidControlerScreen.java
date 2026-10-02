package com.sporeadds.sporeaddsmod.client.screen;

import com.sporeadds.sporeaddsmod.client.gui.RaidControlerMenu;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.StartRaidPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class RaidControlerScreen extends AbstractContainerScreen<RaidControlerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/gui/raid_controler.png");

    private static final int BUTTON_X1 = 115;
    private static final int BUTTON_Y1 = 39;
    private static final int BUTTON_WIDTH = 18;
    private static final int BUTTON_HEIGHT = 21;

    private static final int BUTTON_U_LOCKED = 219;
    private static final int BUTTON_U_HOVER = 238;
    private static final int BUTTON_U_PRESSED = 219;

    private static final int POTENCY_BAIT_MENU_SLOT = 1;

    private static final int EMPTY_SLOT_X = 10;
    private static final int EMPTY_SLOT_Y = 44;
    private static final int EMPTY_SLOT_SIZE = 16;

    private static final int[][] EMPTY_SLOT_FRAMES = {
            {64, 240},
            {80, 240},
            {96, 240}
    };
    private static final int EMPTY_SLOT_FRAME_INTERVAL = 10;

    private boolean wasHoveringButton = false;
    private int pressedButtonTicks = 0;
    private int emptySlotAnimTick = 0;
    private int emptySlotAnimFrame = 0;

    public RaidControlerScreen(RaidControlerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        renderScentDisplay(graphics, x, y);
        renderPotencyDisplay(graphics, x, y);
        renderBiomassBaitDisplay(graphics, x, y);
        renderButton(graphics, x, y, mouseX, mouseY);
    }

    private boolean isPotencyBaitSlotEmpty() {
        return !menu.getSlot(POTENCY_BAIT_MENU_SLOT).hasItem();
    }

    private void renderEmptyInputSlotAnimation(GuiGraphics graphics, int x, int y) {
        if (isPotencyBaitSlotEmpty()) {
            int[] frame = EMPTY_SLOT_FRAMES[emptySlotAnimFrame];

            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.blit(TEXTURE,
                    x + EMPTY_SLOT_X,
                    y + EMPTY_SLOT_Y,
                    frame[0],
                    frame[1],
                    EMPTY_SLOT_SIZE,
                    EMPTY_SLOT_SIZE);
            graphics.pose().popPose();
        }
    }

    private void drawCenteredHalfPixelUp(GuiGraphics graphics, String text, int centerX, int y, int color) {
        int textX = centerX - this.font.width(text) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -0.5F, 0.0F);
        graphics.drawString(this.font, text, textX, y, color);
        graphics.pose().popPose();
    }

    private void renderScentDisplay(GuiGraphics graphics, int x, int y) {
        int count = menu.blockEntity.getScentCount();
        String text = String.valueOf(count);

        int screenLeft = x + 39;
        int screenRight = x + 61;
        int screenTop = y + 23;
        int screenBottom = y + 32;

        int centerX = (screenLeft + screenRight) / 2;
        int centerY = (screenTop + screenBottom) / 2 - 2;

        drawCenteredHalfPixelUp(graphics, text, centerX, centerY, 0xFFFF00);
    }

    private void renderPotencyDisplay(GuiGraphics graphics, int x, int y) {
        int level = menu.blockEntity.getPotencyLevel();
        String text = toRomanNumeral(level);

        if (text.isEmpty()) {
            text = "0";
        }

        int screenLeft = x + 39;
        int screenRight = x + 61;
        int screenTop = y + 47;
        int screenBottom = y + 56;

        int centerX = (screenLeft + screenRight) / 2;
        int centerY = (screenTop + screenBottom) / 2 - 2;

        drawCenteredHalfPixelUp(graphics, text, centerX, centerY, 0x8B0000);
    }

    private void renderBiomassBaitDisplay(GuiGraphics graphics, int x, int y) {
        int count = menu.blockEntity.getBiomassBaitCount();
        String text = String.valueOf(count);

        int screenLeft = x + 39;
        int screenRight = x + 61;
        int screenTop = y + 71;
        int screenBottom = y + 80;

        int centerX = (screenLeft + screenRight) / 2;
        int centerY = (screenTop + screenBottom) / 2 - 2;

        drawCenteredHalfPixelUp(graphics, text, centerX, centerY, 0xFF00FF);
    }

    private String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "";
        };
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int absX1 = x + BUTTON_X1;
        int absY1 = y + BUTTON_Y1;

        boolean canStart = menu.blockEntity.canStartRaid();
        boolean buttonLocked = menu.blockEntity.getCooldownTicks() > 0;
        boolean hovering = canStart
                && mouseX >= absX1 && mouseX < absX1 + BUTTON_WIDTH
                && mouseY >= absY1 && mouseY < absY1 + BUTTON_HEIGHT;

        if (pressedButtonTicks > 0) {
            graphics.blit(TEXTURE, absX1, absY1, BUTTON_U_PRESSED, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else if (buttonLocked) {
            graphics.blit(TEXTURE, absX1, absY1, BUTTON_U_LOCKED, 0, BUTTON_WIDTH, BUTTON_HEIGHT);

            int cooldownTicks = menu.blockEntity.getCooldownTicks();
            int secondsLeft = cooldownTicks / 20 + (cooldownTicks % 20 == 0 ? 0 : 1);
            String text = String.valueOf(secondsLeft);

            int centerX = absX1 + BUTTON_WIDTH / 2;
            int textY = absY1 + 23;
            int textX = centerX - this.font.width(text) / 2;

            graphics.drawString(this.font, text, textX, textY, 0x8B0000);
        } else if (hovering) {
            graphics.blit(TEXTURE, absX1, absY1, BUTTON_U_HOVER, 0, BUTTON_WIDTH, BUTTON_HEIGHT);

            if (!wasHoveringButton) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F)
                );
            }
        }

        wasHoveringButton = hovering;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.blockEntity.canStartRaid()) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;

            int absX1 = x + BUTTON_X1;
            int absY1 = y + BUTTON_Y1;

            boolean hovering = mouseX >= absX1 && mouseX < absX1 + BUTTON_WIDTH
                    && mouseY >= absY1 && mouseY < absY1 + BUTTON_HEIGHT;

            if (hovering) {
                this.pressedButtonTicks = 4;
                NetworkHandle.INSTANCE.sendToServer(new StartRaidPacket(menu.blockEntity.getBlockPos()));

                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                );
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (pressedButtonTicks > 0) {
            pressedButtonTicks--;
        }

        if (isPotencyBaitSlotEmpty()) {
            emptySlotAnimTick++;
            if (emptySlotAnimTick >= EMPTY_SLOT_FRAME_INTERVAL) {
                emptySlotAnimTick = 0;
                emptySlotAnimFrame = (emptySlotAnimFrame + 1) % EMPTY_SLOT_FRAMES.length;
            }
        } else {
            emptySlotAnimTick = 0;
            emptySlotAnimFrame = 0;
        }
    }

    private void renderTooltips(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        if (isWithin(mouseX, mouseY, x + 71, y + 22, x + 83, y + 34)) {
            graphics.renderTooltip(this.font, Component.translatable("gui.sporeadd.raid_controler.scent_info"), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, x + 71, y + 46, x + 83, y + 58)) {
            graphics.renderTooltip(this.font, Component.translatable("gui.sporeadd.raid_controler.potency_info"), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, x + 71, y + 70, x + 83, y + 82)) {
            graphics.renderTooltip(this.font, Component.translatable("gui.sporeadd.raid_controler.biomass_bait_info"), mouseX, mouseY);
        }
    }

    private boolean isWithin(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        renderEmptyInputSlotAnimation(graphics, x, y);
        renderTooltips(graphics, x, y, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}