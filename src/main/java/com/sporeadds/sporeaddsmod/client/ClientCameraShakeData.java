package com.sporeadds.sporeaddsmod.client;

public final class ClientCameraShakeData {

    private static long endTimeMs = 0L;
    private static long startTimeMs = 0L;
    private static float intensity = 0.0F;

    private ClientCameraShakeData() {
    }

    public static void start(int durationTicks, float shakeIntensity) {
        startTimeMs = System.currentTimeMillis();
        endTimeMs = startTimeMs + (durationTicks * 50L);
        intensity = shakeIntensity;
    }

    /**
     * Current shake strength in [0, intensity]: ramps up briefly, holds, then fades out over the last
     * portion of the duration instead of cutting off abruptly.
     */
    public static float getCurrentStrength() {
        long now = System.currentTimeMillis();
        if (now >= endTimeMs || intensity <= 0.0F) {
            return 0.0F;
        }

        long total = endTimeMs - startTimeMs;
        if (total <= 0) {
            return 0.0F;
        }

        float progress = (now - startTimeMs) / (float) total;

        float envelope;
        if (progress < 0.1F) {
            envelope = progress / 0.1F;
        } else if (progress > 0.75F) {
            envelope = (1.0F - progress) / 0.25F;
        } else {
            envelope = 1.0F;
        }

        return intensity * Math.max(0.0F, Math.min(1.0F, envelope));
    }
}
