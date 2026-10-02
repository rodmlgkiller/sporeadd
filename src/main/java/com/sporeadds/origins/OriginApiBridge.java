package com.sporeadds.origins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Placeholder for the Origins integration. The Origins API used on 1.20.1 (edwinmindcraft's Forge port) has no
 * NeoForge 1.21.1 release, so on this branch the bridge does nothing. Callers are already gated behind
 * {@code ModList.get().isLoaded("origins")}, which stays false until an Origins build for 1.21.1 exists; the real
 * implementation can be dropped back in here at that point.
 */
public final class OriginApiBridge {

    private OriginApiBridge() {
    }

    public static void setOrigin(ServerPlayer player, ResourceLocation originId) {
    }

    /** @return the player's current origin in the main layer, or null when unknown. */
    public static ResourceLocation currentMainOrigin(ServerPlayer player) {
        return null;
    }
}
