package com.sporeadds.sporeaddsmod.client;

public final class SelfDefibrillateCooldownClientState {

    private static int remainingTicks = 0;

    private SelfDefibrillateCooldownClientState() {
    }

    public static void startCooldown(int ticks) {
        remainingTicks = ticks;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static boolean isOnCooldown() {
        return remainingTicks > 0;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static int getRemainingSeconds() {
        return (int) Math.ceil(remainingTicks / 20.0D);
    }
}