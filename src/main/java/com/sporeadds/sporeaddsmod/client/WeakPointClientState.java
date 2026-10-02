package com.sporeadds.sporeaddsmod.client;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class WeakPointClientState {

    private static final Map<Integer, Vec3> offsets = new HashMap<>();

    private WeakPointClientState() {
    }

    public static void set(int entityId, Vec3 offset) {
        offsets.put(entityId, offset);
    }

    public static void remove(int entityId) {
        offsets.remove(entityId);
    }

    public static Map<Integer, Vec3> getAll() {
        return offsets;
    }
}