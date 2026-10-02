package com.sporeadds.sporeaddsmod.PlayerData;

import com.sporeadds.sporeaddsmod.capabilities.Capability;

public class ScientistResearchProvider {
    public static final Capability<ScientistResearchData> SCIENTIST_RESEARCH = Capability.ofSimple(
            "scientist_research", holder -> new ScientistResearchData(),
            ScientistResearchData::saveNBTData, ScientistResearchData::loadNBTData);

    private ScientistResearchProvider() {
    }
}
