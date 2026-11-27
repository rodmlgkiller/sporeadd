package com.sporeadds.sporeaddsmod.Powers.berserker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Las 9 "syringes" que el berserker puede colocar en el inventario de Compounds. Cada una
 * otorga un efecto al modo Claws of Brutality y es stackeable segun cuantas del mismo tipo
 * haya colocadas. Todos los items pertenecen al namespace {@code spore}.
 */
public enum CompoundType {
    REINFORCED("reinforced_syringe"),
    SKELETAL("skeletal_syringe"),
    DROWNED("drowned_syringe"),
    VAMPIRIC("vampiric_syringe"),
    CHARRED("charred_syringe"),
    CALCIFIED("calcified_syringe"),
    BEZERK("bezerk_syringe"),
    TOXIC("toxic_syringe"),
    ROTTEN("rotten_syringe");

    private final ResourceLocation itemId;

    CompoundType(String path) {
        this.itemId = new ResourceLocation("spore", path);
    }

    public ResourceLocation itemId() {
        return itemId;
    }

    public String tooltipKey() {
        return "tooltip.sporeadd.compound." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static CompoundType fromItem(Item item) {
        if (item == null) return null;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) return null;
        for (CompoundType type : values()) {
            if (type.itemId.equals(key)) return type;
        }
        return null;
    }

    public static boolean isSyringe(ItemStack stack) {
        return !stack.isEmpty() && fromItem(stack.getItem()) != null;
    }
}
