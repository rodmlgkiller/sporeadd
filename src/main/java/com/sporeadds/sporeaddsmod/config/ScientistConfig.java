package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Scientist class settings: research notes, field research, expose weakness, weak points, prestige.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class ScientistConfig {
    private ScientistConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("scientist");
        builder.pop();
    }
}
