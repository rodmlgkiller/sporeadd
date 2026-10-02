package com.sporeadds.sporeaddsmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

/** Compatibility replacement for Forge's NetworkHooks (menus with a block position payload). */
public final class NetworkHooks {

    private NetworkHooks() {
    }

    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }
}
