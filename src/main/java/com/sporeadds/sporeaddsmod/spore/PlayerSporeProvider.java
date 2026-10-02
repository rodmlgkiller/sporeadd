package com.sporeadds.sporeaddsmod.spore;

import com.sporeadds.sporeaddsmod.capabilities.Capability;

public class PlayerSporeProvider {
    public static final Capability<PlayerSpore> PLAYER_CAP = Capability.ofSimple(
            "player_spore", holder -> new PlayerSpore(), PlayerSpore::saveNBTData, PlayerSpore::loadNBTData);

    private PlayerSporeProvider() {
    }
}
