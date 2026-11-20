package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.client.gui.FieldResearchBookScreen;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public final class FieldResearchClientData {

    private static Map<String, Integer> kills = new HashMap<>();
    private static Map<String, Integer> dataAmounts = new HashMap<>();
    /** Prestige / weak point rules of the server that sent the last book data (null until the first one arrives). */
    private static ScientistConfig.ResearchRules rules = null;

    private FieldResearchClientData() {
    }

    public static void setRules(ScientistConfig.ResearchRules newRules) {
        rules = newRules;
    }

    /** The server's rules for the book numbers; this client's own config if the server never sent any. */
    public static ScientistConfig.ResearchRules getRules() {
        return rules != null ? rules : ScientistConfig.ResearchRules.current();
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