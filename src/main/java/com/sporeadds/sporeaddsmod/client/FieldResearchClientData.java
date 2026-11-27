package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.gui.FieldResearchBookScreen;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public final class FieldResearchClientData {

    private static Map<String, Integer> kills = new HashMap<>();
    private static Map<String, Integer> dataAmounts = new HashMap<>();

    private FieldResearchClientData() {
    }

    public static void setKills(Map<String, Integer> newKills) {
        kills = newKills;
    }

    public static int getKillCount(String entityId) {
        return kills.getOrDefault(entityId, 0);
    }

    public static void setDataAmounts(Map<String, Integer> newData) {
        dataAmounts = newData;
    }

    public static int getDataAmount(String entityId) {
        return dataAmounts.getOrDefault(entityId, 0);
    }

    public static void openBookWhenReady() {
        Minecraft.getInstance().setScreen(new FieldResearchBookScreen());
    }
}