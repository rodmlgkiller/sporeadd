package com.sporeadds.sporeaddsmod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

/** Builds the medic's "Insanity mask" (Spore gas mask with a custom name, lore, curses and armor). */
public final class GasMaskFactory {

    private GasMaskFactory() {
    }

    public static ItemStack decorate(ItemStack gasMask, HolderLookup.Provider access, boolean bindingCurse) {
        gasMask.set(DataComponents.CUSTOM_NAME,
                Component.literal("Insanity mask").withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.DARK_RED)));
        gasMask.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("After seeing the effects of the infection in others you never want to experience it for yourself (You cannot remove your gas mask)")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)))));

        if (bindingCurse) {
            EnchantUtil.add(gasMask, access, Enchantments.BINDING_CURSE, 1);
        }
        EnchantUtil.add(gasMask, access, Enchantments.VANISHING_CURSE, 1);

        gasMask.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        gasMask.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(ResourceLocation.fromNamespaceAndPath("sporeadd", "gas_mask_armor"), 4.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(ResourceLocation.fromNamespaceAndPath("sporeadd", "gas_mask_toughness"), 3.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build());
        return gasMask;
    }
}
