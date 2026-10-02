package com.sporeadds.sporeaddsmod.client.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

public class KommandantPreviewPlayer extends AbstractClientPlayer {

    public KommandantPreviewPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}