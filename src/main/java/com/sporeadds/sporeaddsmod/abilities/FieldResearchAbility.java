package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchData;
import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncScientistResearchPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.LazyOptional;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;

public final class FieldResearchAbility {

    private FieldResearchAbility() {
    }

    public static boolean tryActivate(ServerPlayer player) {
        boolean isScientist = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) {
            return false;
        }

        LazyOptional<ScientistResearchData> capability = player.getCapability(ScientistResearchProvider.SCIENTIST_RESEARCH);

        if (!capability.isPresent()) {
            return false;
        }

        capability.ifPresent(research -> {
            NetworkHandle.INSTANCE.sendTo(
                    new SyncScientistResearchPacket(research.getAllKills(), research.getAllData()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });

        return true;
    }
}