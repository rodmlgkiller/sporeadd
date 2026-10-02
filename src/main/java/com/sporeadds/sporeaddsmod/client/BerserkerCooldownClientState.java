package com.sporeadds.sporeaddsmod.client;

/** Cooldowns de las dos habilidades de berserker (Counter y Claws of Brutality). */
public final class BerserkerCooldownClientState {

    public static final int COUNTER = 0;
    public static final int CLAWS = 1;

    private static int counterTicks = 0;
    private static int clawsTicks = 0;

    private BerserkerCooldownClientState() {
    }

    public static void setCooldown(int which, int ticks) {
        if (which == CLAWS) {
            clawsTicks = Math.max(0, ticks);
        } else {
            counterTicks = Math.max(0, ticks);
        }
    }

    public static void tick() {
        if (counterTicks > 0) counterTicks--;
        if (clawsTicks > 0) clawsTicks--;
    }

    public static boolean isCounterOnCooldown() {
        return counterTicks > 0;
    }

    public static int getCounterTicks() {
        return counterTicks;
    }

    public static boolean isClawsOnCooldown() {
        return clawsTicks > 0;
    }

    public static int getClawsTicks() {
        return clawsTicks;
    }
}
