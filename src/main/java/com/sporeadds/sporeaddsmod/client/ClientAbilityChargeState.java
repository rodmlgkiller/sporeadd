package com.sporeadds.sporeaddsmod.client;

/**
 * Client-only charge state for the shared "key.sporeadd.caustic_shot" (X by default) special ability
 * used by the Caustic and Gluttonous Kommandant subclasses. Driven purely by how long the local player
 * has held the key down - no server round-trip needed since this is only used for the HUD bar.
 */
public final class ClientAbilityChargeState {

    public static final int MAX_TICKS = 75;

    private static int ticks = 0;
    private static boolean charging = false;

    private ClientAbilityChargeState() {
    }

    public static void set(int newTicks, boolean isCharging) {
        ticks = Math.max(0, newTicks);
        charging = isCharging;
    }

    public static int getTicks() {
        return ticks;
    }

    public static boolean isCharging() {
        return charging;
    }

    public static boolean isFullyCharged() {
        return ticks >= MAX_TICKS;
    }

    public static boolean shouldShow() {
        return charging && ticks > 0;
    }
}
