package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchData;
import com.sporeadds.sporeaddsmod.PlayerData.ScientistResearchProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.ScientistConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncScientistResearchPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FieldResearchAbility {

    /** Game time at which each player may open Field Research again (only used when the cooldown is above 0). */
    private static final Map<UUID, Long> COOLDOWN_UNTIL_TICK = new HashMap<>();

    private FieldResearchAbility() {
    }

    public static boolean tryActivate(ServerPlayer player) {
        boolean isScientist = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) {
            return false;
        }

        LazyOptional<ScientistResearchData> capability = player.getCapability(ScientistResearchProvider.SCIENTIST_RESEARCH);

        if (!capability.isPresent()) {
            return false;
        }

        // scientist.field_research.cooldown_ticks: 0 (default) = no cooldown, it opens any time.
        long cooldownTicks = ScientistConfig.fieldResearchCooldownTicks();
        if (cooldownTicks > 0) {
            long now = player.level().getGameTime();
            Long until = COOLDOWN_UNTIL_TICK.get(player.getUUID());

            if (until != null && now < until) {
                int secondsLeft = (int) ((until - now) / 20L) + 1;
                player.displayClientMessage(
                        Component.translatable("ability.sporeadds.scientist.field_research_cooldown", secondsLeft)
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
                return false;
            }

            COOLDOWN_UNTIL_TICK.put(player.getUUID(), now + cooldownTicks);
        }

        capability.ifPresent(research -> {
            // The packet also carries the prestige / weak point rules and the cooldown so the book (and the HUD)
            // show what THIS server uses.
            NetworkHandle.INSTANCE.sendTo(
                    new SyncScientistResearchPacket(research.getAllKills(), research.getAllData(), ScientistConfig.ResearchRules.current()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });

        return true;
    }
}