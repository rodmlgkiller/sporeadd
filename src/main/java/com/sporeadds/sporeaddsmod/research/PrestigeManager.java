package com.sporeadds.sporeaddsmod.research;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import net.minecraft.server.level.ServerPlayer;

public final class PrestigeManager {

    private PrestigeManager() {
    }

    public record Requirement(int kills, int data) {
    }

    public static Requirement getRequirement(int rarity) {
        return switch (rarity) {
            case 1 -> new Requirement(20, 500);
            case 2 -> new Requirement(10, 250);
            case 3 -> new Requirement(5, 125);
            case 4 -> new Requirement(2, 50);
            default -> new Requirement(Integer.MAX_VALUE, Integer.MAX_VALUE);
        };
    }

    public static boolean isPrestiged(ServerPlayer player, String entityId) {
        int rarity = TrackedEntities.getRarity(entityId);
        Requirement req = getRequirement(rarity);

        return ScientistResearchProvider.SCIENTIST_RESEARCH.get(player)
                .map(research -> research.getKillCount(entityId) >= req.kills()
                        && research.getDataAmount(entityId) >= req.data())
                .orElse(false);
    }

    public static boolean isPrestigedByValues(String entityId, int kills, int data) {
        int rarity = TrackedEntities.getRarity(entityId);
        Requirement req = getRequirement(rarity);
        return kills >= req.kills() && data >= req.data();
    }

    public static boolean isPrestigedByAnyScientist(net.minecraft.server.MinecraftServer server, String entityId) {
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            boolean isScientist = com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider.SPORE_IDENTIFIER.get(online).map(data -> "scientist".equalsIgnoreCase(data.getIdentifier())).orElse(false);

            if (!isScientist) {
                continue;
            }

            if (isPrestiged(online, entityId)) {
                return true;
            }
        }
        return false;
    }
}