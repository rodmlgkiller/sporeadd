package com.sporeadds.sporeaddsmod.client;

import java.util.HashSet;
import java.util.Set;

public final class CamouflageClientState {

    private static final Set<Integer> CAMOUFLAGED_ENTITIES = new HashSet<>();

    private CamouflageClientState() {
    }

    public static void setCamouflaged(int entityId, boolean camouflaged) {
        if (camouflaged) {
            CAMOUFLAGED_ENTITIES.add(entityId);
        } else {
            CAMOUFLAGED_ENTITIES.remove(entityId);
        }
    }

    public static boolean isCamouflaged(int entityId) {
        return CAMOUFLAGED_ENTITIES.contains(entityId);
    }
}