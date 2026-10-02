package com.sporeadds.sporeaddsmod.client.hive;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;

/**
 * Valores de config de la colmena que el servidor manda al cliente (los COMMON de Forge no
 * se sincronizan solos). Hasta recibir el paquete se usa el config local como fallback.
 */
public final class HiveClientConfig {

    private static Double syncedChance = null;

    private HiveClientConfig() {
    }

    public static void setChance(double chance) {
        syncedChance = chance;
    }

    public static void clear() {
        syncedChance = null;
    }

    public static double getChance() {
        return syncedChance != null ? syncedChance : SporeAddsConfig.CALL_OF_THE_HIVE_CHANCE.get();
    }

    /** Porcentaje sin ceros de más: 0.05 -> "5", 0.025 -> "2.5". */
    public static String getChancePercentText() {
        double pct = getChance() * 100.0D;
        if (pct == Math.rint(pct)) {
            return String.valueOf((long) pct);
        }
        return String.valueOf(Math.round(pct * 100.0D) / 100.0D);
    }
}
