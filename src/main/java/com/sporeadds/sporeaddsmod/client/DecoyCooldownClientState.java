package com.sporeadds.sporeaddsmod.client;

public final class DecoyCooldownClientState {

    private static int remainingTicks = 0;

    private DecoyCooldownClientState() {
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
}