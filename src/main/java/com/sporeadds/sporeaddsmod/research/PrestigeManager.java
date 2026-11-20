package com.sporeadds.sporeaddsmod.research;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import net.minecraft.server.level.ServerPlayer;

public final class PrestigeManager {

    private PrestigeManager() {
    }

    public record Requirement(int kills, int data) {
    }

    /** Requirements of that rarity (1-4) from this side's config (scientist.prestige.rarity_N). */
    public static Requirement getRequirement(int rarity) {
        if (rarity < 1 || rarity > ScientistConfig.PRESTIGE_RARITIES) {
            return new Requirement(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        return new Requirement(
                ScientistConfig.PRESTIGE_KILLS[rarity - 1].get(),
                ScientistConfig.PRESTIGE_DATA[rarity - 1].get()
        );
    }

    /** Same, but from a rules snapshot (what the Field Research book uses: the numbers the server sent). */
    public static Requirement getRequirement(int rarity, ScientistConfig.ResearchRules rules) {
        return new Requirement(rules.prestigeKills(rarity), rules.prestigeData(rarity));
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

    /** Same, using a rules snapshot instead of this side's config (used by the Field Research book). */
    public static boolean isPrestigedByValues(String entityId, int kills, int data, ScientistConfig.ResearchRules rules) {
        int rarity = TrackedEntities.getRarity(entityId);
        Requirement req = getRequirement(rarity, rules);
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