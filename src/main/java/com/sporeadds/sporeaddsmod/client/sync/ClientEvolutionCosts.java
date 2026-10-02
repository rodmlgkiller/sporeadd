package com.sporeadds.sporeaddsmod.client.sync;

public class ClientEvolutionCosts {
    private static final String[] COSTS = new String[10]; // 1..9

    public static void setCost(int level, String value) {
        if (level >= 1 && level <= 9) {
            COSTS[level] = value;
        }
    }

    public static void setAll(String[] values) {
        for (int i = 1; i <= 9 && i < values.length; i++) {
            COSTS[i] = values[i];
        }
    }

    public static String getCost(int level) {
        if (level < 1 || level > 9) return "() cost:50";
        return COSTS[level] != null ? COSTS[level] : "() cost:50";
    }
}