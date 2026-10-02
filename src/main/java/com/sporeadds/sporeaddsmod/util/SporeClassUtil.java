package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.world.entity.player.Player;

public final class SporeClassUtil {

    private SporeClassUtil() {
    }

    public static boolean hasClass(Player player, String classId) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> classId.equals(data.getIdentifier()))
                .orElse(false);
    }
}