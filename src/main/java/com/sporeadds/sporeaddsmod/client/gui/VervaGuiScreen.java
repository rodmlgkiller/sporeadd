package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SpawnVervaPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VervaGuiScreen extends Screen {

    public enum Category {
        INFECTED("gui.sporeadds.verva.category.infected"),
        ORGANOIDS("gui.sporeadds.verva.category.organoids"),
        BOMBS("gui.sporeadds.verva.category.bombs");

        public final String translationKey;

        Category(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    private static final Set<String> FAVORITE_MOBS = new HashSet<>();
    private static final double TARGET_GUI_SCALE = 3.0D;

    private static class MobEntry {
        String id;
        String displayName;
        int baseCost;
        int forcedVariant;
        int[] variantCosts;
        int originalIndex;
        boolean isFavorite;

        LivingEntity cacheLivingEntity;
        Entity cacheEntity;

        float damage;
        int radius;
        boolean carrier;
        float bombScale;

        MobEntry(String id, String displayName, int baseCost, int forcedVariant, int[] variantCosts, int originalIndex) {
            this(id, displayName, baseCost, forcedVariant, variantCosts, originalIndex, 10.0F, 5, false, 1.0F);
        }

        MobEntry(String id, String displayName, int baseCost, int forcedVariant, int[] variantCosts, int originalIndex, float damage, int radius, boolean carrier, float bombScale) {
            this.id = id;
            this.displayName = displayName;
            this.baseCost = baseCost;
            this.forcedVariant = forcedVariant;
            this.variantCosts = variantCosts;
            this.originalIndex = originalIndex;
            this.damage = damage;
            this.radius = radius;
            this.carrier = carrier;
            this.bombScale = bombScale;
            this.isFavorite = FAVORITE_MOBS.contains(id);
        }
    }

    private Category currentCategory = Category.INFECTED;
    private final List<MobEntry> allMobs = new ArrayList<>();
    private final List<MobEntry> filteredMobs = new ArrayList<>();

    private final List<String> serverVerwaMenu;
    private final List<String> serverOrganoidMenu;
    private final List<String> serverBombMenu;

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 12;

    private EditBox searchBox;
    private String searchText = "";

    private final Button[] gridButtons = new Button[12];
    private final Button[] starButtons = new Button[12];
    private Button prevButton;
    private Button nextButton;

    private int playerSporeLevel = 0;

    private int scaledWidth;
    private int scaledHeight;

    public VervaGuiScreen(List<String> verwaMenu, List<String> organoidMenu, List<String> bombMenu) {
        super(Component.translatable("gui.sporeadds.verva.title"));

        this.serverVerwaMenu = verwaMenu;
        this.serverOrganoidMenu = organoidMenu;
        this.serverBombMenu = bombMenu;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> {
                this.playerSporeLevel = cap.getLevel();
            });
        }

        loadCategory(Category.INFECTED);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                this.searchBox.setFocused(false);
                return true;
            }
            return this.searchBox.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (com.sporeadds.sporeaddsmod.client.SporeKeyMapping.OPEN_VERVA_MENU.isActiveAndMatches(
                InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        double scaleFactor = getScaleFactor();
        this.scaledWidth = (int) (this.width / scaleFactor);
        this.scaledHeight = (int) (this.height / scaleFactor);

        int startX = (this.scaledWidth - 240) / 2;
        int startY = 70;
        int tabsStartX = startX + 2;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.sporeadds.verva.category.infected"), b -> {
            loadCategory(Category.INFECTED);
            updateGrid();
        }).bounds(tabsStartX, 10, 75, 20).build());

        Button organoidsButton = Button.builder(Component.translatable("gui.sporeadds.verva.category.organoids"), b -> {
            loadCategory(Category.ORGANOIDS);
            updateGrid();
        }).bounds(tabsStartX + 80, 10, 75, 20).build();
        organoidsButton.active = playerSporeLevel >= SporeAddsConfig.ORGANOID_MENU_UNLOCK_LEVEL.get();
        this.addRenderableWidget(organoidsButton);

        Button bombsButton = Button.builder(Component.translatable("gui.sporeadds.verva.category.bombs"), b -> {
            loadCategory(Category.BOMBS);
            updateGrid();
        }).bounds(tabsStartX + 160, 10, 75, 20).build();
        bombsButton.active = playerSporeLevel >= SporeAddsConfig.BOMB_MENU_UNLOCK_LEVEL.get();
        this.addRenderableWidget(bombsButton);

        this.searchBox = new EditBox(this.font, startX, 40, 240, 20, Component.translatable("gui.sporeadds.verva.search"));
        this.searchBox.setValue(this.searchText);
        this.searchBox.setResponder(text -> {
            this.searchText = text;
            updateSearch();
            updateGrid();
        });
        this.addRenderableWidget(this.searchBox);

        for (int i = 0; i < 12; i++) {
            final int index = i;
            int col = i % 4;
            int row = i / 4;
            int x = startX + (col * 60) + 5;
            int y = startY + (row * 70);

            gridButtons[i] = Button.builder(Component.empty(), b -> {
                MobEntry entry = getEntryForIndex(index);
                if (entry != null) {
                    boolean forceVariantMenu = entry.id.equals("spore:inf_player")
                            || entry.id.equals("spore:umarmed")
                            || entry.id.equals("spore:usurper")
                            || entry.id.equals("spore:braurei")
                            || entry.id.equals("spore:delusioner");

                    boolean hasVariants = entry.cacheLivingEntity instanceof VariantKeeper || forceVariantMenu;

                    if (entry.forcedVariant >= 0) {
                        NetworkHandle.INSTANCE.sendToServer(
                                new SpawnVervaPacket(
                                        entry.id, entry.baseCost, entry.forcedVariant,
                                        entry.damage, entry.radius, entry.carrier, entry.bombScale,
                                        entry.displayName
                                )
                        );
                    } else if (hasVariants) {
                        int variantCount = entry.id.equals("spore:inf_player")
                                ? 3
                                : (entry.cacheLivingEntity instanceof VariantKeeper keeper ? keeper.amountOfMutations() : 1);

                        this.minecraft.setScreen(
                                new VervaVariantGuiScreen(this, entry.id, entry.baseCost, entry.variantCosts, variantCount)
                        );
                    } else {
                        NetworkHandle.INSTANCE.sendToServer(new SpawnVervaPacket(entry.id, entry.baseCost, -1));
                    }
                }
            }).bounds(x, y, 50, 60).build();
            this.addRenderableWidget(gridButtons[i]);

            starButtons[i] = Button.builder(Component.literal("☆"), b -> {
                MobEntry entry = getEntryForIndex(index);
                if (entry != null) {
                    entry.isFavorite = !entry.isFavorite;
                    if (entry.isFavorite) FAVORITE_MOBS.add(entry.id);
                    else FAVORITE_MOBS.remove(entry.id);
                    updateSearch();
                    updateGrid();
                }
            }).bounds(x + 36, y + 2, 12, 12).build();
            this.addRenderableWidget(starButtons[i]);
        }

        prevButton = Button.builder(Component.translatable("gui.sporeadds.verva.prev"), b -> {
            if (currentPage > 0) {
                currentPage--;
                updateGrid();
            }
        }).bounds(startX, this.scaledHeight - 30, 50, 20).build();

        nextButton = Button.builder(Component.translatable("gui.sporeadds.verva.next"), b -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < filteredMobs.size()) {
                currentPage++;
                updateGrid();
            }
        }).bounds(startX + 190, this.scaledHeight - 30, 50, 20).build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
        updateGrid();
    }

    private void loadCategory(Category cat) {
        this.currentCategory = cat;
        this.allMobs.clear();
        this.currentPage = 0;
        int index = 0;

        if (cat == Category.INFECTED) {
            for (String entry : this.serverVerwaMenu) {
                String[] parts = entry.split(";");
                if (parts.length >= 2) {
                    String mobId = parts[0].trim();
                    try {
                        int baseCost = Integer.parseInt(parts[1].trim());
                        int[] variantCosts = null;
                        if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                            String[] varStrs = parts[2].split(",");
                            variantCosts = new int[varStrs.length];
                            for (int i = 0; i < varStrs.length; i++) {
                                variantCosts[i] = Integer.parseInt(varStrs[i].trim());
                            }
                        }
                        allMobs.add(new MobEntry(mobId, mobId, baseCost, -1, variantCosts, index++));
                    } catch (Exception ignored) {
                    }
                }
            }
        } else if (cat == Category.ORGANOIDS) {
            for (String entry : this.serverOrganoidMenu) {
                String[] parts = entry.split(";");
                if (parts.length >= 2) {
                    try {
                        allMobs.add(new MobEntry(parts[0].trim(), parts[0].trim(), Integer.parseInt(parts[1].trim()), -1, null, index++));
                    } catch (Exception ignored) {
                    }
                }
            }
        } else if (cat == Category.BOMBS) {
            for (String entry : this.serverBombMenu) {
                String[] parts = entry.split(";");
                if (parts.length >= 8) {
                    try {
                        allMobs.add(new MobEntry(
                                parts[0].trim(),
                                parts[1].trim(),
                                Integer.parseInt(parts[2].trim()),
                                Integer.parseInt(parts[3].trim()),
                                null,
                                index++,
                                Float.parseFloat(parts[4].trim()),
                                Integer.parseInt(parts[5].trim()),
                                Boolean.parseBoolean(parts[6].trim()),
                                Float.parseFloat(parts[7].trim())
                        ));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        updateSearch();
    }

    private void updateSearch() {
        filteredMobs.clear();
        String lowerSearch = searchText.toLowerCase();
        for (MobEntry mob : allMobs) {
            if (mob.id.toLowerCase().contains(lowerSearch) || mob.displayName.toLowerCase().contains(lowerSearch)) {
                filteredMobs.add(mob);
            }
        }
        filteredMobs.sort((m1, m2) -> {
            if (m1.isFavorite != m2.isFavorite) return m1.isFavorite ? -1 : 1;
            return Integer.compare(m1.originalIndex, m2.originalIndex);
        });
        currentPage = 0;
    }

    private MobEntry getEntryForIndex(int index) {
        int listIndex = (currentPage * ITEMS_PER_PAGE) + index;
        if (listIndex >= 0 && listIndex < filteredMobs.size()) return filteredMobs.get(listIndex);
        return null;
    }

    private void updateGrid() {
        for (int i = 0; i < 12; i++) {
            MobEntry entry = getEntryForIndex(i);
            if (entry != null) {
                gridButtons[i].visible = true;
                starButtons[i].visible = true;
                starButtons[i].setMessage(Component.literal(entry.isFavorite ? "★" : "☆")
                        .withStyle(entry.isFavorite ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
            } else {
                gridButtons[i].visible = false;
                starButtons[i].visible = false;
            }
        }
        prevButton.active = currentPage > 0;
        nextButton.active = (currentPage + 1) * ITEMS_PER_PAGE < filteredMobs.size();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)  {

        double scaleFactor = getScaleFactor();
        PoseStack pose = graphics.pose();

        pose.pushPose();
        pose.scale((float) scaleFactor, (float) scaleFactor, 1.0f);

        int adjustedMouseX = (int) adjustMouseX(mouseX);
        int adjustedMouseY = (int) adjustMouseY(mouseY);

        super.render(graphics, adjustedMouseX, adjustedMouseY, partialTick);

        PoseStack topPose = graphics.pose();
        topPose.pushPose();
        topPose.translate(0.0D, 0.0D, 200.0D);
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredMobs.size() / ITEMS_PER_PAGE));
        graphics.drawCenteredString(
                this.font,
                Component.translatable("gui.sporeadds.verva.page", currentPage + 1, totalPages),
                this.scaledWidth / 2,
                this.scaledHeight - 25,
                0xAAAAAA
        );
        topPose.popPose();

        int startX = (this.scaledWidth - 240) / 2;
        int startY = 70;

        for (int i = 0; i < 12; i++) {
            MobEntry entry = getEntryForIndex(i);
            if (entry != null) {
                int col = i % 4;
                int row = i / 4;
                int x = startX + (col * 60) + 5;
                int y = startY + (row * 70);

                if (entry.cacheEntity == null && this.minecraft != null && this.minecraft.level != null) {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry.id));
                    if (type != null) {
                        Entity entity = type.create(this.minecraft.level);
                        if (entity != null) {
                            if (entity instanceof FleshBomb bomb && entry.forcedVariant >= 0) {
                                bomb.setBombType(entry.forcedVariant);
                                bomb.setCarrier(entry.carrier);
                            }
                            entry.cacheEntity = entity;
                            if (entity instanceof LivingEntity living) {
                                entry.cacheLivingEntity = living;
                            }
                        }
                    }
                }

                if (entry.cacheEntity instanceof FleshBomb bomb) {
                    int scale = (int) (14 * entry.bombScale);
                    renderAnyEntity(graphics, x + 25, y + 38, scale, bomb, partialTick);
                } else if (entry.cacheLivingEntity != null) {
                    int scale = entry.cacheLivingEntity.getBbHeight() > 2.0f ? 8 : 15;
                    com.sporeadds.sporeaddsmod.util.EntityPreview.renderFollowsMouse(
                            graphics,
                            x + 25,
                            y + 38,
                            scale,
                            x + 25 - adjustedMouseX,
                            y + 25 - adjustedMouseY,
                            entry.cacheLivingEntity
                    );
                }

                PoseStack textPose = graphics.pose();
                textPose.pushPose();
                textPose.translate(0.0D, 0.0D, 200.0D);

                if (currentCategory == Category.BOMBS) {
                    graphics.drawCenteredString(this.font, entry.displayName, x + 25, y + 2, 0xFFFFFF);
                }

                graphics.drawCenteredString(
                        this.font,
                        Component.translatable("gui.sporeadds.verva.biomass_cost", entry.baseCost),
                        x + 25,
                        y + 50,
                        0xAA0000
                );
                textPose.popPose();
            }
        }

        if (currentCategory == Category.BOMBS) {
            int textX = startX + 240 + 20;
            int textY = startY + 20;
            Component infoText = Component.translatable("gui.sporeadds.bombs_info");
            graphics.drawWordWrap(this.font, infoText, textX, textY, 120, 0xFF5555);
        }

        pose.popPose();
    }

    private void renderAnyEntity(GuiGraphics graphics, int x, int y, int scale, Entity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PoseStack pose = graphics.pose();

        pose.pushPose();
        pose.translate(x, y - 17.0D, 75.0D);
        pose.mulPose(new Quaternionf().rotateZ((float) Math.PI).rotateX((float) Math.toRadians(90.0D)));
        pose.scale((float) scale, (float) scale, (float) -scale);

        RenderSystem.enableDepthTest();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        dispatcher.setRenderShadow(false);

        try {
            dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, pose, buffer, 15728880);
            buffer.endBatch();
        } catch (Exception ignored) {
        }

        dispatcher.setRenderShadow(true);
        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);

        for (int i = 0; i < starButtons.length; i++) {
            Button starButton = starButtons[i];
            if (starButton != null && starButton.visible && starButton.active && starButton.isMouseOver(adjX, adjY)) {
                return starButton.mouseClicked(adjX, adjY, button);
            }
        }

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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        return super.mouseScrolled(adjX, adjY, scrollX, scrollY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double adjX = adjustMouseX(mouseX);
        double adjY = adjustMouseY(mouseY);
        super.mouseMoved(adjX, adjY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}