package com.sporeadds.sporeaddsmod.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Qué jugadores (por id de entidad) tienen las garras de Claws of Brutality activas, para el render. */
public final class BerserkerClawRenderState {

    private static final Set<Integer> ACTIVE = ConcurrentHashMap.newKeySet();

    private BerserkerClawRenderState() {
    }

    public static void set(int entityId, boolean active) {
        if (active) {
            ACTIVE.add(entityId);
        } else {
            ACTIVE.remove(entityId);
        }
    }

    public static boolean isActive(int entityId) {
        return ACTIVE.contains(entityId);
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
