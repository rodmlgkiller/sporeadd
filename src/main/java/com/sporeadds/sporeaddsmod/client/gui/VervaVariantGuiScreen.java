package com.sporeadds.sporeaddsmod.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SpawnVervaPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VervaVariantGuiScreen extends Screen {

    private final Screen parentScreen;
    private final String mobId;
    private final int baseCost;
    private final int[] variantCosts;
    private final int variantCount;
    private LivingEntity[] cacheEntities;

    private static final String[] LOADOUT_NAMES = { "Random", "Archer", "Swordsman" };
    private static final double TARGET_GUI_SCALE = 3.0D;

    private int scaledWidth;
    private int scaledHeight;

    public VervaVariantGuiScreen(Screen parentScreen, String mobId, int baseCost, int[] variantCosts, int variantCount) {
        super(Component.literal("Select Variant for " + mobId));
        this.parentScreen = parentScreen;
        this.mobId = mobId;
        this.baseCost = baseCost;
        this.variantCosts = variantCosts;

        if (mobId.equals("spore:inf_player")) {
            this.variantCount = LOADOUT_NAMES.length;
        } else {
            this.variantCount = variantCount;
        }

        this.cacheEntities = new LivingEntity[this.variantCount];
    }

    private int getCostForVariant(int index) {
        if (variantCosts != null && index < variantCosts.length) {
            return variantCosts[index];
        }
        return baseCost;
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

        this.addRenderableWidget(Button.builder(Component.literal("< Back"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(parentScreen);
            }
        }).bounds(10, 10, 50, 20).build());

        int startX = (this.scaledWidth - (variantCount * 60)) / 2;
        int startY = (this.scaledHeight - 70) / 2;

        for (int i = 0; i < variantCount; i++) {
            final int variantIndex = i;
            final int displayedCost = getCostForVariant(variantIndex);

            int x = startX + (i * 60);
            int y = startY;

            Button btn = Button.builder(Component.empty(), b -> {
                // El cliente envía el coste mostrado, pero el servidor debe recalcularlo
                // usando mobId + variantIndex y su propia config.
                NetworkHandle.INSTANCE.sendToServer(new SpawnVervaPacket(this.mobId, displayedCost, variantIndex));
            }).bounds(x, y, 50, 60).build();

            this.addRenderableWidget(btn);
        }
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

        boolean isInfectedPlayer = this.mobId.equals("spore:inf_player");
        String title = isInfectedPlayer ? "Select Loadout" : "Select Variant";

        graphics.drawCenteredString(this.font, title, this.scaledWidth / 2, 20, 0xFFFFFF);

        int startX = (this.scaledWidth - (variantCount * 60)) / 2;
        int startY = (this.scaledHeight - 70) / 2;

        for (int i = 0; i < variantCount; i++) {
            int x = startX + (i * 60);
            int y = startY;

            if (cacheEntities[i] == null && this.minecraft != null && this.minecraft.level != null) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.mobId));
                if (type != null) {
                    Entity entity = type.create(this.minecraft.level);
                    if (entity instanceof LivingEntity living) {
                        if (isInfectedPlayer) {
                            applyVisualLoadout(living, i);
                        } else if (living instanceof VariantKeeper keeper) {
                            keeper.setVariant(i);
                        }
                        cacheEntities[i] = living;
                    }
                }
            }

            if (cacheEntities[i] != null) {
                int scale = 15;
                if (cacheEntities[i].getBbHeight() > 2.0f) scale = 8;

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics,
                        x + 25,
                        y + 45,
                        scale,
                        x + 25 - adjustedMouseX,
                        y + 25 - adjustedMouseY,
                        cacheEntities[i]
                );
            }

            String label = isInfectedPlayer ? LOADOUT_NAMES[i] : "Var " + i;
            graphics.drawCenteredString(this.font, label, x + 25, y - 10, 0xAAAAAA);

            int currentCost = getCostForVariant(i);
            graphics.drawCenteredString(this.font, currentCost + " Biomass", x + 25, y + 50, 0xAA0000);
        }

        pose.popPose();
    }

    private void applyVisualLoadout(LivingEntity entity, int loadoutId) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }

        switch (loadoutId) {
            case 0 -> {
            }
            case 1 -> {
                equipVisual(entity, EquipmentSlot.HEAD, "minecraft:leather_helmet");
                equipVisual(entity, EquipmentSlot.CHEST, "minecraft:leather_chestplate");
                equipVisual(entity, EquipmentSlot.LEGS, "minecraft:leather_leggings");
                equipVisual(entity, EquipmentSlot.FEET, "minecraft:leather_boots");
                equipVisual(entity, EquipmentSlot.MAINHAND, "minecraft:bow");
            }
            case 2 -> {
                equipVisual(entity, EquipmentSlot.HEAD, "minecraft:iron_helmet");
                equipVisual(entity, EquipmentSlot.CHEST, "minecraft:iron_chestplate");
                equipVisual(entity, EquipmentSlot.LEGS, "minecraft:chainmail_leggings");
                equipVisual(entity, EquipmentSlot.FEET, "minecraft:iron_boots");
                equipVisual(entity, EquipmentSlot.MAINHAND, "minecraft:iron_sword");
                equipVisual(entity, EquipmentSlot.OFFHAND, "minecraft:shield");
            }
        }
    }

    private void equipVisual(LivingEntity entity, EquipmentSlot slot, String itemId) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        if (item != null && item != Items.AIR) {
            entity.setItemSlot(slot, new ItemStack(item));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
}