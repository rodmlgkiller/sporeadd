package com.sporeadds.sporeaddsmod.client;

public class KommandantBrightnessState {

    private static double currentGamma = 1.0D;
    private static boolean forcedByKommandant = false;

    public static double getCurrentGamma() {
        return currentGamma;
    }

    public static void setCurrentGamma(double gamma) {
        currentGamma = gamma;
    }

    public static boolean isForcedByKommandant() {
        return forcedByKommandant;
    }

    public static void setForcedByKommandant(boolean forced) {
        forcedByKommandant = forced;
    }

    public static void enable(double gamma) {
        currentGamma = gamma;
        forcedByKommandant = true;
    }

    public static void disable() {
        forcedByKommandant = false;
    }
}