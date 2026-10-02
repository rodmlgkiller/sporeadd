package com.sporeadds.sporeaddsmod.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extra render-only scale (1.0-1.5) applied to a just-fired Caustic assassin bullet, keyed by entity
 * id, based on how charged the shot was. Purely a client-side rendering hint - never touches the
 * entity's actual hitbox/collision box. Entries are short-lived (the bullet discards itself on
 * impact almost immediately) and are pruned by age as a safety net.
 */
public final class ClientCausticShotScaleState {

    private static final Map<Integer, Float> SCALES = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> SET_AT = new ConcurrentHashMap<>();
    private static final long MAX_AGE_MS = 10_000L;

    private ClientCausticShotScaleState() {
    }

    public static void set(int entityId, float scale) {
        SCALES.put(entityId, scale);
        SET_AT.put(entityId, System.currentTimeMillis());
        if (SCALES.size() > 64) {
            prune();
        }
    }

    public static float get(int entityId) {
        Float scale = SCALES.get(entityId);
        return scale == null ? 1.0F : scale;
    }

    private static void prune() {
        long now = System.currentTimeMillis();
        SET_AT.entrySet().removeIf(entry -> {
            boolean stale = now - entry.getValue() > MAX_AGE_MS;
            if (stale) SCALES.remove(entry.getKey());
            return stale;
        });
    }
}
