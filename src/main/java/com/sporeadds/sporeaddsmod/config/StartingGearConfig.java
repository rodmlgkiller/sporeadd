package com.sporeadds.sporeaddsmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Starting gear given to each class when it is chosen.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class StartingGearConfig {
    private StartingGearConfig() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("starting_gear");
        builder.pop();
    }
}
