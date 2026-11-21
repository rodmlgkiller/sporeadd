package com.sporeadds.sporeaddsmod.client.config;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client only: registers the Mods > SporeAdds > Config screen.
 *
 * <p>This lives in a class of its own on purpose. {@code SporeAddsMod.ClientInit} is loaded and verified on a dedicated server too
 * ({@code DistExecutor.safeRunWhenOn} checks its method reference), and the JVM verifier loads {@code Screen} when it checks that the
 * lambda below returns a Screen; a dedicated server refuses to load that class and the mod then fails to construct. Here the
 * lambda is only ever linked on the client.
 */
public final class SporeConfigScreenRegistration {
    private SporeConfigScreenRegistration() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new SporeConfigScreen(parent)));
    }
}
