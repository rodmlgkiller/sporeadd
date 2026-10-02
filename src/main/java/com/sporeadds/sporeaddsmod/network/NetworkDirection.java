package com.sporeadds.sporeaddsmod.network;

import net.neoforged.fml.LogicalSide;

/** Compatibility replacement for Forge's NetworkDirection (only the two play directions are used). */
public enum NetworkDirection {
    PLAY_TO_SERVER(LogicalSide.SERVER),
    PLAY_TO_CLIENT(LogicalSide.CLIENT);

    private final LogicalSide receptionSide;

    NetworkDirection(LogicalSide receptionSide) {
        this.receptionSide = receptionSide;
    }

    public LogicalSide getReceptionSide() {
        return receptionSide;
    }
}
