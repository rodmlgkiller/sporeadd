package com.sporeadds.sporeaddsmod.items;

import com.sporeadds.sporeaddsmod.config.ImplantConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImplantItem extends Item {
    private final String tooltipKey;

    // Al construir el ítem, le pasamos qué texto de traducción debe usar
    public ImplantItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    /** "tooltip.sporeadds.implant.left_leg" -> "left_leg": the implant_stats sub-section of this implant. */
    private String implantTypeId() {
        int dot = tooltipKey.lastIndexOf('.');
        return dot >= 0 ? tooltipKey.substring(dot + 1) : tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (Screen.hasShiftDown()) {
            // El texto descriptivo del implante en color Verde y cursiva
            tooltip.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
            // Valores cambiados en el config (implant_stats.<tipo>): el texto de arriba describe los números originales.
            tooltip.addAll(ImplantConfig.describeChanges(implantTypeId()));
            // Un pequeño aviso de que es un implante cibernético
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.sporeadds.implant_type").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.sporeadds.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}