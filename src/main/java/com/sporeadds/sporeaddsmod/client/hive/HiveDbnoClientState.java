package com.sporeadds.sporeaddsmod.client.hive;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro en cliente de qué jugadores (por id de entidad) están en modo "downed but not out"
 * de la colmena, para que {@code PlayerDbnoRenderMixin} los dibuje tumbados en el suelo.
 */
public final class HiveDbnoClientState {

    private static final Set<Integer> DOWNED = ConcurrentHashMap.newKeySet();

    private HiveDbnoClientState() {
    }

    public static void setDowned(int entityId, boolean downed) {
        if (downed) {
            DOWNED.add(entityId);
        } else {
            DOWNED.remove(entityId);
        }
    }

    public static boolean isDowned(int entityId) {
        return DOWNED.contains(entityId);
    }

    public static void clear() {
        DOWNED.clear();
    }
}
