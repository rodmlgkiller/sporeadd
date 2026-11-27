package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchData;
import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncScientistResearchPacket;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;

public final class FieldResearchAbility {

    private FieldResearchAbility() {
    }

    public static boolean tryActivate(ServerPlayer player) {
        boolean isScientist = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) {
            return false;
        }

        LazyOptional<ScientistResearchData> capability = ScientistResearchProvider.SCIENTIST_RESEARCH.get(player);

        if (!capability.isPresent()) {
            return false;
        }

        capability.ifPresent(research -> {
            NetworkHandle.INSTANCE.sendTo(
                    new SyncScientistResearchPacket(research.getAllKills(), research.getAllData()),
                    player.connection.getConnection(),
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });

        return true;
    }
}