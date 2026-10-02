package com.sporeadds.sporeaddsmod.PlayerData;

import com.sporeadds.sporeaddsmod.capabilities.Capability;

public class SporeIdentifierProvider {
    public static final Capability<SporeIdentifierData> SPORE_IDENTIFIER = Capability.ofSimple(
            "spore_identifier", holder -> new SporeIdentifierData(),
            SporeIdentifierData::saveNBTData, SporeIdentifierData::loadNBTData);

    private SporeIdentifierProvider() {
    }
}
