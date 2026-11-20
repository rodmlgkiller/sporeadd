package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Kommandant abilities 8-13 (horde, infectious burst, proto tracking, vigil, biomass armor, nuke).
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class KommandantAdvancedConfig {
    private KommandantAdvancedConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("kommandant_abilities_advanced");
        builder.pop();
    }
}
