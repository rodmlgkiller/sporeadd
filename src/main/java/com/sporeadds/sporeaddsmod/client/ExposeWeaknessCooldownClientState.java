package com.sporeadds.sporeaddsmod.client;

public final class ExposeWeaknessCooldownClientState {

    private static int remainingTicks = 0;

    private ExposeWeaknessCooldownClientState() {
    }

    public static void setCooldown(int ticks) {
        remainingTicks = ticks;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static boolean isOnCooldown() {
        return remainingTicks > 0;
    }
}