package com.sporeadds.sporeaddsmod.client.trainingbook;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.RequestClassCountsPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public class TrainingBookScreen extends Screen {

    static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("textures/gui/book.png");
    static final int IMAGE_WIDTH = 192;
    static final int IMAGE_HEIGHT = 192;
    static final int TEXT_LEFT_OFFSET = 36;
    static final int TEXT_TOP_OFFSET = 30;
    static final int TEXT_WIDTH = 114;
    static final int TEXT_HEIGHT = 128;
    static final int LINE_HEIGHT = 10;

    private final InteractionHand hand;

    /** Fila de la página principal: cabecera de categoría, o una clase seleccionable. */
    private record Row(boolean header, String value) {}

    private List<Row> rows;

    int leftPos;
    int topPos;

    public TrainingBookScreen(InteractionHand hand) {
        super(Component.translatable("screen.sporeadd.training_book.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;

        List<String> humans = new ArrayList<>();
        boolean hasKommandant = false;
        for (String id : SporeIdentifierData.VALID_IDS) {
            if (id.equals("none") || !SporeAddsConfig.isClassEnabledInTrainingBook(id)) {
                continue;
            }
            if (id.equals("kommandant")) {
                hasKommandant = true;
            } else {
                humans.add(id);
            }
        }

        this.rows = new ArrayList<>();
        if (!humans.isEmpty()) {
            rows.add(new Row(true, "humans"));
            for (String h : humans) {
                rows.add(new Row(false, h));
            }
        }
        if (hasKommandant) {
            rows.add(new Row(true, "spore"));
            rows.add(new Row(false, "kommandant"));
        }

        NetworkHandle.INSTANCE.sendToServer(new RequestClassCountsPacket());
    }

    private void openClassDetail(String classId) {
        if (this.minecraft != null) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.setScreen(new TrainingBookClassDetailScreen(classId, hand, this));
        }
    }

    private int rowTop(int index) {
        return topPos + TEXT_TOP_OFFSET + 16 + index * LINE_HEIGHT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        graphics.blit(BOOK_TEXTURE, this.leftPos, this.topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        int centerX = this.leftPos + TEXT_LEFT_OFFSET + TEXT_WIDTH / 2;

        drawFittedCenteredNoShadow(graphics, this.font, this.title, centerX, this.topPos + TEXT_TOP_OFFSET, TEXT_WIDTH, 0x000000);

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int y = rowTop(i);

            if (row.header()) {
                Component header = Component.translatable("screen.sporeadd.training_book.category." + row.value())
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD);
                graphics.drawCenteredString(this.font, header, centerX, y, 0x000000);
                continue;
            }

            String classId = row.value();
            Component name = Component.translatable("class.sporeadd." + classId)
                    .withStyle(TrainingBookColors.getColor(classId));

            boolean hovered = isRowHovered(mouseX, mouseY, i);
            if (hovered) {
                name = name.copy().withStyle(ChatFormatting.UNDERLINE);
            }

            graphics.drawCenteredString(this.font, name, centerX, y, hovered ? 0xFFFFFF : 0x000000);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    static void drawCenteredNoShadow(GuiGraphics graphics, Font font, Component text, int centerX, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, centerX - width / 2, y, color, false);
    }

    /**
     * Draws centered text without shadow, shrinking it (never enlarging) so it never overflows maxWidth.
     */
    static void drawFittedCenteredNoShadow(GuiGraphics graphics, Font font, Component text, int centerX, int y, int maxWidth, int color) {
        int width = font.width(text);
        if (width <= maxWidth || width <= 0) {
            drawCenteredNoShadow(graphics, font, text, centerX, y, color);
            return;
        }

        float scale = maxWidth / (float) width;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, -width / 2, 0, color, false);
        graphics.pose().popPose();
    }

    private boolean isRowHovered(int mouseX, int mouseY, int index) {
        if (index < 0 || index >= rows.size() || rows.get(index).header()) {
            return false;
        }
        int y = rowTop(index);
        return mouseX >= leftPos + TEXT_LEFT_OFFSET && mouseX <= leftPos + TEXT_LEFT_OFFSET + TEXT_WIDTH
                && mouseY >= y && mouseY <= y + LINE_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < rows.size(); i++) {
                if (isRowHovered((int) mouseX, (int) mouseY, i)) {
                    openClassDetail(rows.get(i).value());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
