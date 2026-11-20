package com.sporeadds.sporeaddsmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Core Mound Locator, Improvised Locator and Proto Locator: cost items and cooldowns.
 *
 * <p>Defaults reproduce the behaviour that was hard-coded before these options existed. Read values with
 * {@code .get()} at USE time (never copy them into static finals: the config is not loaded yet while classes
 * initialise). Called from {@link SporeAddsConfig}'s static initialiser, before the spec is built.
 */
public final class LocatorConfig {
    private LocatorConfig() {
    }

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("locators");
        builder.pop();
    }
}
