package com.sporeadds.sporeaddsmod.client;

/** Estado (solo del jugador local) del contador de "Brutality" y de si las garras están activas. */
public final class ClawCounterClientState {

    public static final float MAX = 200.0F;

    private static float value = 0.0F;
    private static boolean active = false;

    private ClawCounterClientState() {
    }

    public static void set(float newValue, boolean isActive) {
        value = Math.max(0.0F, Math.min(MAX, newValue));
        active = isActive;
    }

    public static float getValue() {
        return value;
    }

    public static boolean isActive() {
        return active;
    }

    /** true si hay algo que mostrar en el HUD (contador con valor, o garras activas). */
    public static boolean shouldShow() {
        return active || value > 0.0F;
    }
}
