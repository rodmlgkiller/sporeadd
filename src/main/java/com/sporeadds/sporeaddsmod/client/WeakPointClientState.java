package com.sporeadds.sporeaddsmod.client;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WeakPointClientState {

    /**
     * The weak points of one entity as the server described them: offsets from the entity position, the tier (sprite)
     * and the weak point size (hit radius in blocks, the marker is scaled with it).
     */
    public record Marker(List<Vec3> offsets, int tier, float radius) {
    }

    private static final Map<Integer, Marker> markers = new HashMap<>();

    private WeakPointClientState() {
    }

    public static void set(int entityId, List<Vec3> offsets, int tier, float radius) {
        markers.put(entityId, new Marker(List.copyOf(offsets), tier, radius));
    }

    /** One weak point with the default tier and size. */
    public static void set(int entityId, Vec3 offset) {
        markers.put(entityId, new Marker(List.of(offset), 0, 0.3F));
    }

    public static void remove(int entityId) {
        markers.remove(entityId);
    }

    public static Map<Integer, Marker> getAll() {
        return markers;
    }
}