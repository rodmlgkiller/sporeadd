package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Surgical implants: the attributes and amounts each implant grants.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class ImplantConfig {
    private ImplantConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("implant_stats");
        builder.pop();
    }
}
