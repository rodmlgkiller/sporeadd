package com.sporeadds.sporeaddsmod.level;

import com.sporeadds.sporeaddsmod.capabilities.Capability;
import com.sporeadds.sporeaddsmod.network.SyncLevelPacket;
import net.minecraft.server.level.ServerPlayer;

public class PlayerLevelProvider {
    public static final Capability<PlayerLevel> PLAYER_LVL = Capability.ofSimple(
            "player_level",
            holder -> {
                ServerPlayer attachedPlayer = holder instanceof ServerPlayer sp ? sp : null;
                return new PlayerLevel() {
                    @Override
                    public void setLevel(int newLevel) {
                        super.setLevel(newLevel);
                        if (attachedPlayer != null) {
                            SyncLevelPacket.syncLevelToClient(attachedPlayer);
                        }
                    }

                    @Override
                    public void addLevel(int value) {
                        super.addLevel(value);
                        if (attachedPlayer != null) {
                            SyncLevelPacket.syncLevelToClient(attachedPlayer);
                        }
                    }

                    @Override
                    public void setKnowledgeLevel(int newKnowledgeLevel) {
                        super.setKnowledgeLevel(newKnowledgeLevel);
                        if (attachedPlayer != null) {
                            SyncLevelPacket.syncLevelToClient(attachedPlayer);
                        }
                    }
                };
            },
            PlayerLevel::saveNBTData, PlayerLevel::loadNBTData);

    private PlayerLevelProvider() {
    }
}
