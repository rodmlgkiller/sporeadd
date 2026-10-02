package com.sporeadds.sporeaddsmod.client.event;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.Powers.berserker.CompoundType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Muestra a los jugadores con la clase berserker qué efecto tiene cada "syringe" cuando se
 * coloca en el inventario de Compounds (habilidad Claws of Brutality).
 */
@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CompoundTooltipEvents {

    private static boolean isBerserker(Player player) {
        if (player == null) return false;
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
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
