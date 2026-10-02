package com.sporeadds.sporeaddsmod.client;

import java.util.HashSet;
import java.util.Set;

public class ClientCamouflageState {

    private static final Set<Integer> CAMOUFLAGED_ENTITY_IDS = new HashSet<>();

    private ClientCamouflageState() {
    }

    public static void setCamouflaged(int entityId, boolean camouflaged) {
        if (camouflaged) {
            CAMOUFLAGED_ENTITY_IDS.add(entityId);
        } else {
            CAMOUFLAGED_ENTITY_IDS.remove(entityId);
        }
    }

    public static boolean isCamouflaged(int entityId) {
        return CAMOUFLAGED_ENTITY_IDS.contains(entityId);
    }
}