package com.sporeadds.sporeaddsmod.research;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchData;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.ResearchPopupPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public final class ResearchEventHelper {

    private ResearchEventHelper() {
    }

    public static void recordKill(ServerPlayer scientist, ScientistResearchData research, String entityId) {
        boolean wasUnlocked = research.getKillCount(entityId) > 0;
        boolean wasPrestiged = PrestigeManager.isPrestigedByValues(
                entityId, research.getKillCount(entityId), research.getDataAmount(entityId));

        research.incrementKill(entityId);

        boolean isPrestigedNow = PrestigeManager.isPrestigedByValues(
                entityId, research.getKillCount(entityId), research.getDataAmount(entityId));

        if (!wasUnlocked) {
            sendPopup(scientist, ResearchPopupType.UNLOCK, entityId, 0);
        }

        if (!wasPrestiged && isPrestigedNow) {
            sendPopup(scientist, ResearchPopupType.PRESTIGE, entityId, 0);
        }
    }

    public static void recordData(ServerPlayer scientist, ScientistResearchData research, String entityId, int amount) {
        boolean wasPrestiged = PrestigeManager.isPrestigedByValues(
                entityId, research.getKillCount(entityId), research.getDataAmount(entityId));

        research.addData(entityId, amount);

        boolean isPrestigedNow = PrestigeManager.isPrestigedByValues(
                entityId, research.getKillCount(entityId), research.getDataAmount(entityId));

        sendPopup(scientist, ResearchPopupType.DATA, entityId, amount);

        if (!wasPrestiged && isPrestigedNow) {
            sendPopup(scientist, ResearchPopupType.PRESTIGE, entityId, 0);
        }
    }

    private static void sendPopup(ServerPlayer player, ResearchPopupType type, String entityId, int amount) {
        NetworkHandle.INSTANCE.sendTo(
                new ResearchPopupPacket(type, entityId, amount),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}