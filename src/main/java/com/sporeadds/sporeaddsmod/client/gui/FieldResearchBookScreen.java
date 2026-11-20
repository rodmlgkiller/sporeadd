package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.client.FieldResearchClientData;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import com.sporeadds.sporeaddsmod.research.TrackedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class FieldResearchBookScreen extends Screen {

    private static final double TARGET_GUI_SCALE = 4.0D;
    private static final int UNLOCKED_NAME_COLOR = 0x8B0000;
    private static final int WEAK_POINT_SPRITE_SIZE = 8;

    private static class EntryCache {
        String id;
        LivingEntity entity;
    }

    private final List<EntryCache> entries = new ArrayList<>();
    private int currentPage = 0;
    private Button prevButton;
    private Button nextButton;

    private int scaledWidth;
    private int scaledHeight;

    private boolean openSoundPlayed = false;

    public FieldResearchBookScreen() {
        super(Component.translatable("gui.sporeadds.field_research.title"));

        for (String id : TrackedEntities.ENTITY_IDS) {
            EntryCache entry = new EntryCache();
            entry.id = id;
            entries.add(entry);
        }
    }

    private double getScaleFactor() {
        Minecraft mc = Minecraft.getInstance();
        double currentScale = mc.getWindow().getGuiScale();
        return TARGET_GUI_SCALE / currentScale;
    }

    private double adjustMouseX(double mouseX) {
        return mouseX / getScaleFactor();
    }

    private double adjustMouseY(double mouseY) {
        return mouseY / getScaleFactor();
    }

    private void playPageTurnSound() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            SoundEvents.BOOK_PAGE_TURN, 1.0F
                    )
            );
        }
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        if (!openSoundPlayed && this.minecraft != null) {
            openSoundPlayed = true;
            this.minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            SoundEvents.BOOK_PUT, 1.0F
                    )
            );
        }

        double scaleFactor = getScaleFactor();
        this.scaledWidth = (int) (this.width / scaleFactor);
        this.scaledHeight = (int) (this.height / scaleFactor);

        int centerX = this.scaledWidth / 2;
        int bottomY = this.scaledHeight - 30;

        prevButton = Button.builder(Component.literal("<"), b -> {
            if (currentPage > 0) {
                currentPage--;
                playPageTurnSound();
            }
        }).bounds(centerX - 100, bottomY, 40, 20).build();

        nextButton = Button.builder(Component.literal(">"), b -> {
            if ((currentPage + 1) * 2 * entriesPerPage() < entries.size()) {
                currentPage++;
                playPageTurnSound();
            }
        }).bounds(centerX + 60, bottomY, 40, 20).build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        double scaleFactor = getScaleFactor();
        PoseStack pose = graphics.pose();

        pose.pushPose();
        pose.scale((float) scaleFactor, (float) scaleFactor, 1.0f);

        int adjustedMouseX = (int) adjustMouseX(mouseX);
        int adjustedMouseY = (int) adjustMouseY(mouseY);

        super.render(graphics, adjustedMouseX, adjustedMouseY, partialTick);

        int centerX = this.scaledWidth / 2;
        int pageTop = 40;

        int leftPageX = centerX - 130;
        int rightPageX = centerX + 10;
        int pageWidth = 130;
        int pageHeight = 170;

        int perPage = entriesPerPage();
        int perSpread = perPage * 2;

        // The config can change while the book is open: keep the current page inside the book.
        int lastPage = Math.max(0, (entries.size() - 1) / perSpread);
        if (currentPage > lastPage) {
            currentPage = lastPage;
        }

        int firstIndex = currentPage * perSpread;

        if (perPage == 1) {
            // Original layout: one entry per page.
            renderPageBackground(graphics, leftPageX, pageTop, pageWidth, pageHeight, firstIndex);
            renderPageBackground(graphics, rightPageX, pageTop, pageWidth, pageHeight, firstIndex + 1);

            renderEntry(graphics, leftPageX, pageTop, pageWidth, pageHeight, adjustedMouseX, adjustedMouseY, partialTick, firstIndex);
            renderEntry(graphics, rightPageX, pageTop, pageWidth, pageHeight, adjustedMouseX, adjustedMouseY, partialTick, firstIndex + 1);
        } else {
            renderPageGrid(graphics, leftPageX, pageTop, pageWidth, pageHeight, adjustedMouseX, adjustedMouseY, partialTick, firstIndex, perPage);
            renderPageGrid(graphics, rightPageX, pageTop, pageWidth, pageHeight, adjustedMouseX, adjustedMouseY, partialTick, firstIndex + perPage, perPage);
        }

        prevButton.active = currentPage > 0;
        nextButton.active = (currentPage + 1) * perSpread < entries.size();

        pose.popPose();
    }

    /**
     * scientist.field_research.entries_per_page (CLIENT side config): how many entries each page shows. 1 = the original
     * big entry per page; more entries are drawn as smaller cards in a grid.
     */
    private int entriesPerPage() {
        return Math.max(1, ScientistConfig.fieldResearchEntriesPerPage());
    }

    /**
     * Draws {@code perPage} entries (starting at {@code firstIndex}) as a grid of scaled-down cards inside one page area.
     * Each card is the normal full-size entry (same drawing code) scaled to fit its cell.
     */
    private void renderPageGrid(GuiGraphics graphics, int pageX, int pageY, int pageWidth, int pageHeight,
                                int mouseX, int mouseY, float partialTick, int firstIndex, int perPage) {
        int cols = (int) Math.ceil(Math.sqrt(perPage));
        int rows = (int) Math.ceil((double) perPage / cols);

        float cellWidth = (float) pageWidth / cols;
        float cellHeight = (float) pageHeight / rows;
        float scale = Math.min(cellWidth / pageWidth, cellHeight / pageHeight);

        PoseStack pose = graphics.pose();

        for (int i = 0; i < perPage; i++) {
            int index = firstIndex + i;
            if (index >= entries.size()) {
                break;
            }

            int col = i % cols;
            int row = i / cols;

            float cellX = pageX + col * cellWidth + (cellWidth - pageWidth * scale) / 2.0F;
            float cellY = pageY + row * cellHeight + (cellHeight - pageHeight * scale) / 2.0F;

            int localMouseX = (int) ((mouseX - cellX) / scale);
            int localMouseY = (int) ((mouseY - cellY) / scale);

            pose.pushPose();
            pose.translate(cellX, cellY, 0.0F);
            pose.scale(scale, scale, 1.0F);

            renderPageBackground(graphics, 0, 0, pageWidth, pageHeight, index);
            renderEntry(graphics, 0, 0, pageWidth, pageHeight, localMouseX, localMouseY, partialTick, index);

            pose.popPose();
        }
    }

    private void renderPageBackground(GuiGraphics graphics, int x, int y, int width, int height, int index) {
        int rarity = 1;
        String entityId = null;

        if (index < entries.size()) {
            entityId = entries.get(index).id;
            rarity = TrackedEntities.getRarity(entityId);
        }

        ResourceLocation texture = TrackedEntities.getPageTexture(rarity);
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);

        if (entityId != null && isPrestiged(entityId)) {
            graphics.blit(TrackedEntities.GOLDEN_FRAME_TEXTURE, x, y, 0, 0, width, height, width, height);
        }
    }

    private boolean isPrestiged(String entityId) {
        int kills = FieldResearchClientData.getKillCount(entityId);
        int data = FieldResearchClientData.getDataAmount(entityId);
        return com.sporeadds.sporeaddsmod.research.PrestigeManager.isPrestigedByValues(
                entityId, kills, data, FieldResearchClientData.getRules());
    }

    private void renderEntry(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick, int index) {
        if (index >= entries.size()) {
            return;
        }

        EntryCache entry = entries.get(index);
        boolean hasKilled = FieldResearchClientData.getKillCount(entry.id) > 0;

        if (entry.entity == null && this.minecraft != null && this.minecraft.level != null) {
            if (TrackedEntities.isKommandantPlayerEntry(entry.id)) {
                com.mojang.authlib.GameProfile profile = this.minecraft.player != null
                        ? this.minecraft.player.getGameProfile()
                        : new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Kommandant");

                KommandantPreviewPlayer fakePlayer = new KommandantPreviewPlayer(
                        (net.minecraft.client.multiplayer.ClientLevel) this.minecraft.level,
                        profile
                );

                fakePlayer.getCapability(com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER)
                        .ifPresent(data -> data.setIdentifier("kommandant"));

                entry.entity = fakePlayer;
            } else {
                EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entry.id));
                if (type != null) {
                    Entity created = type.create(this.minecraft.level);
                    if (created instanceof LivingEntity living) {
                        entry.entity = living;
                    }
                }
            }
        }

        int renderCenterX = x + width / 2;
        int renderCenterY = y + 70;

        if (entry.entity != null) {
            if (!hasKilled) {
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
            }

            int baseScale = entry.entity.getBbHeight() > 2.0f ? 12 : 20;
            int finalScale = TrackedEntities.isHalfScaleRender(entry.id) ? baseScale / 2 : baseScale;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    renderCenterX,
                    renderCenterY,
                    finalScale,
                    renderCenterX - mouseX,
                    renderCenterY - mouseY,
                    entry.entity
            );

            if (!hasKilled) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        Component nameText;
        if (TrackedEntities.isKommandantPlayerEntry(entry.id)) {
            nameText = hasKilled
                    ? Component.translatable("gui.sporeadds.field_research.kommandant_player")
                    : Component.literal("???");
        } else if (hasKilled) {
            String translationKey = "entity." + entry.id.replace(":", ".");
            nameText = Component.translatable(translationKey);
        } else {
            nameText = Component.literal("???");
        }

        int nameColor = hasKilled ? UNLOCKED_NAME_COLOR : 0x000000;
        graphics.drawCenteredString(this.font, nameText, renderCenterX, y + 105, nameColor);

        if (hasKilled) {
            int killCount = FieldResearchClientData.getKillCount(entry.id);
            Component countText = Component.translatable("gui.sporeadds.field_research.kill_count", killCount);
            graphics.drawCenteredString(this.font, countText, renderCenterX, y + 117, 0x555555);

            int dataAmount = FieldResearchClientData.getDataAmount(entry.id);
            Component dataText = Component.translatable("gui.sporeadds.field_research.data_amount", dataAmount);
            graphics.drawCenteredString(this.font, dataText, renderCenterX, y + 129, 0xFFD700);

            renderWeakPointInfo(graphics, entry.id, killCount, dataAmount, renderCenterX, y + 141);
        }
    }

    private void renderWeakPointInfo(GuiGraphics graphics, String entityId, int killCount, int dataAmount, int centerX, int lineY) {
        boolean isPlayerEntry = TrackedEntities.isKommandantPlayerEntry(entityId);

        // The numbers behind the bonus come from the server (sent with the book data): kill value, soft cap, flat/percent...
        ScientistConfig.ResearchRules rules = FieldResearchClientData.getRules();
        int rawValue = dataAmount + (killCount * rules.killValueInData);

        float damageBonus = com.sporeadds.sporeaddsmod.combat.WeakPointManager.getDamageBonusFromRaw(rawValue, isPlayerEntry, rules);
        int tier = com.sporeadds.sporeaddsmod.combat.WeakPointManager.computeTier(damageBonus);
        int tierColor = com.sporeadds.sporeaddsmod.combat.WeakPointManager.getTierColor(tier);
        ResourceLocation sprite = com.sporeadds.sporeaddsmod.combat.WeakPointManager.getTierTexture(tier);

        // FLAT: "+3.5 dmg on crit". PERCENT mode: the same line with the percentage: "+35% dmg on crit".
        float shownBonus = com.sporeadds.sporeaddsmod.combat.WeakPointManager.displayBonus(damageBonus, rules);
        Component damageText = Component.translatable(
                "gui.sporeadds.field_research.weak_point_damage",
                rules.percentMode ? String.format("%.0f%%", shownBonus) : String.format("%.1f", shownBonus)
        );

        int textWidth = this.font.width(damageText);
        int totalWidth = textWidth + WEAK_POINT_SPRITE_SIZE + 2;
        int startX = centerX - totalWidth / 2;

        graphics.drawString(this.font, damageText, startX, lineY, tierColor, false);
        graphics.blit(sprite, startX + textWidth + 2, lineY - 1, 0, 0, WEAK_POINT_SPRITE_SIZE, WEAK_POINT_SPRITE_SIZE, WEAK_POINT_SPRITE_SIZE, WEAK_POINT_SPRITE_SIZE);

        boolean prestiged = com.sporeadds.sporeaddsmod.research.PrestigeManager.isPrestigedByValues(entityId, killCount, dataAmount, rules);
        if (prestiged) {
            Component prestigeText = Component.translatable("gui.sporeadds.field_research.prestige_bonus");
            graphics.drawCenteredString(this.font, prestigeText, centerX, lineY + 10, 0xFFD700);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        return super.mouseClicked(adjX, adjY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        return super.mouseReleased(adjX, adjY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        double adjDragX = adjustMouseX(dragX);
        double adjDragY = adjustMouseY(dragY);
        return super.mouseDragged(adjX, adjY, button, adjDragX, adjDragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        return super.mouseScrolled(adjX, adjY, delta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        super.mouseMoved(adjX, adjY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}