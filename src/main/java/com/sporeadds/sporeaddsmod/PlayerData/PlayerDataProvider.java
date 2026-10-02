package com.sporeadds.sporeaddsmod.PlayerData;

import com.sporeadds.sporeaddsmod.capabilities.Capability;
import net.minecraft.world.entity.player.Player;

public class PlayerDataProvider {
    public static final Capability<PlayerData> PLAYER_DATA = Capability.ofSimple(
            "player_data", holder -> new PlayerData(), PlayerData::saveNBTData, PlayerData::loadNBTData);

    private PlayerDataProvider() {
    }

    public static PlayerData get(Player player) {
        return PLAYER_DATA.get(player).orElseThrow(() ->
                new IllegalStateException("Missing PlayerData capability for player " + player.getName().getString()));
    }
}
