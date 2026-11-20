package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Kommandant abilities 1-7 (evolution/cocoon, infection, mounds, ESP, harvest and feeding, second authority, flight) and general Kommandant rules.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class KommandantPowersConfig {
    private KommandantPowersConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("kommandant_abilities");
        builder.pop();
    }
}
