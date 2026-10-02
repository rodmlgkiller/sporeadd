package com.sporeadds.sporeaddsmod.client;

import com.sporeadds.sporeaddsmod.network.SyncGasSpheresPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side cache of active caustic gas spheres, keyed by the anchor mound's entity id.
 * Server updates only arrive every ~10 ticks (see CausticGasBombTracker), so the radius is
 * interpolated between the last two known values over wall-clock time instead of snapping,
 * the same way vanilla smooths entity movement between network updates.
 */
public final class ClientGasSphereData {

    private static final long INTERP_DURATION_MS = 550L;

    private static final Map<Integer, Tracked> TRACKED = new HashMap<>();

    private ClientGasSphereData() {
    }

    public static void update(List<SyncGasSpheresPacket.Sphere> newSpheres) {
        long now = System.currentTimeMillis();
        java.util.Set<Integer> seen = new java.util.HashSet<>();

        for (SyncGasSpheresPacket.Sphere sphere : newSpheres) {
            seen.add(sphere.entityId);

            Tracked tracked = TRACKED.get(sphere.entityId);
            if (tracked == null) {
                tracked = new Tracked();
                tracked.prevRadius = sphere.radius;
                TRACKED.put(sphere.entityId, tracked);
            } else {
                tracked.prevRadius = currentRadius(tracked, now);
            }

            tracked.x = sphere.x;
            tracked.y = sphere.y;
            tracked.z = sphere.z;
            tracked.targetRadius = sphere.radius;
            tracked.updateTimeMs = now;
        }

        TRACKED.keySet().removeIf(id -> !seen.contains(id));
    }

    private static float currentRadius(Tracked tracked, long now) {
        float t = Mth.clamp((now - tracked.updateTimeMs) / (float) INTERP_DURATION_MS, 0.0F, 1.0F);
        return Mth.lerp(t, tracked.prevRadius, tracked.targetRadius);
    }

    public static List<RenderSphere> getRenderSpheres() {
        long now = System.currentTimeMillis();
        List<RenderSphere> result = new ArrayList<>(TRACKED.size());
        for (Tracked tracked : TRACKED.values()) {
            result.add(new RenderSphere(tracked.x, tracked.y, tracked.z, currentRadius(tracked, now)));
        }
        return result;
    }

    public static boolean isInsideAny(Vec3 pos) {
        long now = System.currentTimeMillis();
        for (Tracked tracked : TRACKED.values()) {
            double dx = pos.x - tracked.x;
            double dy = pos.y - tracked.y;
            double dz = pos.z - tracked.z;
            float radius = currentRadius(tracked, now);
            if (dx * dx + dy * dy + dz * dz <= (double) radius * radius) {
                return true;
            }
        }
        return false;
    }

    private static class Tracked {
        double x;
        double y;
        double z;
        float prevRadius;
        float targetRadius;
        long updateTimeMs;
    }

    public static class RenderSphere {
        public final double x;
        public final double y;
        public final double z;
        public final float radius;

        public RenderSphere(double x, double y, double z, float radius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }
}
