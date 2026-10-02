package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.client.trainingbook.TrainingBookColors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ClassTooltipUtil {

    private ClassTooltipUtil() {
    }

    /**
     * Builds the "Requires class: X" / "Usable by any class" tooltip line shown on class-exclusive items,
     * matching whatever the item's own "requires origin" config currently allows.
     */
    public static Component classRequirement(boolean requiresOrigin, String classId) {
        if (!requiresOrigin) {
            return Component.translatable("tooltip.sporeadd.class_required.any").withStyle(ChatFormatting.YELLOW);
        }

        Component className = Component.translatable("class.sporeadd." + classId)
                .withStyle(TrainingBookColors.getColor(classId));

        return Component.translatable("tooltip.sporeadd.class_required", className).withStyle(ChatFormatting.YELLOW);
    }
}
