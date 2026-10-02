package com.sporeadds.sporeaddsmod.client.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Muestra a los jugadores con la clase berserker qué efecto tiene cada "syringe" cuando se
 * coloca en el inventario de Compounds (habilidad Claws of Brutality).
 */
@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class CompoundTooltipEvents {

    private static boolean isBerserker(Player player) {
        if (player == null) return false;
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "berserker".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            player = Minecraft.getInstance().player;
        }
        if (player == null || !isBerserker(player)) return;

        CompoundType type = CompoundType.fromItem(event.getItemStack().getItem());
        if (type == null) return;

        event.getToolTip().add(Component.translatable("tooltip.sporeadd.compound.header")
                .withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable(type.tooltipKey())
                .withStyle(ChatFormatting.GREEN));
    }
}
