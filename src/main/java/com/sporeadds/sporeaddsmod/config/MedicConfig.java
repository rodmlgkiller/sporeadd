package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Medic class settings: injector, self-injection, defibrillation and self-defibrillation.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class MedicConfig {
    private MedicConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("medic");
        builder.pop();
    }
}
