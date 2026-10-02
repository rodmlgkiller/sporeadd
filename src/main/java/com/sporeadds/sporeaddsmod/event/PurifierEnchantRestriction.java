package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.items.PortableAirPurifierItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * The Portable Air Purifier is in the durability enchantable tag (so Unbreaking applies), which would also let
 * Mending through the anvil. Only Unbreaking is allowed on it.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class PurifierEnchantRestriction {

    private PurifierEnchantRestriction() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!(event.getLeft().getItem() instanceof PortableAirPurifierItem)) {
            return;
        }
        if (hasOtherThanUnbreaking(event.getRight())) {
            event.setCanceled(true);
        }
    }

    private static boolean hasOtherThanUnbreaking(ItemStack stack) {
        return hasOther(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY))
                || hasOther(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
    }

    private static boolean hasOther(ItemEnchantments enchantments) {
        return enchantments.keySet().stream().anyMatch(holder -> !holder.is(Enchantments.UNBREAKING));
    }
}
