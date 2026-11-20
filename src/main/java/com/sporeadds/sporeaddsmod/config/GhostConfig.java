package com.sporeadds.sporeaddsmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Ghost class settings: backstab, disguise, decoy, bucket of remains, execution, critical wound.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class GhostConfig {
    private GhostConfig() {
    }

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("ghost");
        builder.pop();
    }
}
