package com.sporeadds.sporeaddsmod.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

/** Keeps the pre-1.21 "feet position + mouse offset" call style for the inventory-style entity previews. */
public final class EntityPreview {

    private static final float Y_OFFSET = 0.0625F;

    private EntityPreview() {
    }

    /**
     * @param centerX  horizontal centre of the preview
     * @param feetY    y of the entity's feet
     * @param relMouseX centre minus mouse x (how far the mouse is from the preview), as the old API took it
     * @param relMouseY feet minus mouse y
     */
    public static void renderFollowsMouse(GuiGraphics graphics, int centerX, int feetY, int scale,
                                          float relMouseX, float relMouseY, LivingEntity entity) {
        int centerY = Math.round(feetY - (entity.getBbHeight() / 2.0F + Y_OFFSET) * scale);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, centerX, centerY, centerX, centerY, scale, Y_OFFSET,
                centerX - relMouseX, centerY - relMouseY, entity);
    }
}
