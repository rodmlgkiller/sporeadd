package com.sporeadds.sporeaddsmod.client.trainingbook;

import net.minecraft.ChatFormatting;

import java.util.Map;

public final class TrainingBookColors {

    private static final Map<String, ChatFormatting> COLORS = Map.of(
            "kommandant", ChatFormatting.DARK_RED,
            "medic", ChatFormatting.RED,
            "scientist", ChatFormatting.AQUA,
            "ghost", ChatFormatting.GREEN,
            "berserker", ChatFormatting.GOLD
    );

    private TrainingBookColors() {
    }

    public static ChatFormatting getColor(String classId) {
        return COLORS.getOrDefault(classId == null ? "" : classId.toLowerCase(), ChatFormatting.GRAY);
    }
}
