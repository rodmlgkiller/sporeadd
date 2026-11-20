package com.sporeadds.sporeaddsmod.client;

/** Estado (solo del jugador local) del contador de "Brutality" y de si las garras están activas. */
public final class ClawCounterClientState {

    /** Maximo por defecto (= config berserker.claws.max_points); el servidor envia el valor configurado. */
    public static final float MAX = 200.0F;

    private static float value = 0.0F;
    private static boolean active = false;
    private static float max = MAX;
    private static float activationThreshold = MAX;

    private ClawCounterClientState() {
    }

    /** Actualiza valor y estado conservando la escala (maximo / umbral) ya recibida. */
    public static void set(float newValue, boolean isActive) {
        set(newValue, isActive, max, activationThreshold);
    }

    /** Actualiza valor, estado y escala de la barra (maximo y umbral de activacion que envia el servidor). */
    public static void set(float newValue, boolean isActive, float newMax, float newActivationThreshold) {
        max = newMax > 0.0F ? newMax : MAX;
        activationThreshold = (newActivationThreshold > 0.0F && newActivationThreshold <= max) ? newActivationThreshold : max;
        value = Math.max(0.0F, Math.min(max, newValue));
        active = isActive;
    }

    public static float getValue() {
        return value;
    }

    public static boolean isActive() {
        return active;
    }

    /** Escala completa de la barra (config berserker.claws.max_points del servidor). */
    public static float getMax() {
        return max;
    }

    /** Puntos necesarios para activar las garras (config berserker.claws.activation_threshold del servidor). */
    public static float getActivationThreshold() {
        return activationThreshold;
    }

    /** true si hay algo que mostrar en el HUD (contador con valor, o garras activas). */
    public static boolean shouldShow() {
        return active || value > 0.0F;
    }
}
