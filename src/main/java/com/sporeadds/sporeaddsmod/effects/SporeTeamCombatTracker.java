package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "sporeadd")
public final class SporeTeamCombatTracker {

    private static final String SPORE_TEAM_NAME = "spore";
    private static final String GHOST_IDENTIFIER = "ghost";

    private static final Set<UUID> HIT_BY_SPORE_NON_GHOST = new HashSet<>();

    private SporeTeamCombatTracker() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        if (isSporeTeamNonGhost(livingAttacker)) {
            HIT_BY_SPORE_NON_GHOST.add(player.getUUID());
        }
    }

    public static boolean wasHitBySporeNonGhost(UUID playerId) {
        return HIT_BY_SPORE_NON_GHOST.contains(playerId);
    }

    public static void clear(UUID playerId) {
        HIT_BY_SPORE_NON_GHOST.remove(playerId);
    }

    /**
     * Un atacante cuenta como "equipo spore, no ghost" si:
     * - Es un jugador: su team de scoreboard es "spore" y su identifier NO es "ghost".
     * - Es un mob (no jugador): su team de scoreboard es "spore".
     *   Los ghosts son siempre jugadores (clase), así que un mob nunca es "ghost".
     */
    public static boolean isSporeTeamNonGhost(LivingEntity attacker) {
        PlayerTeam team = attacker.getTeam() instanceof PlayerTeam pt ? pt : null;
        boolean isOnSporeTeam = team != null && SPORE_TEAM_NAME.equalsIgnoreCase(team.getName());

        if (!isOnSporeTeam) {
            return false;
        }

        if (attacker instanceof Player attackerPlayer) {
            boolean isGhost = attackerPlayer.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                    .map(data -> GHOST_IDENTIFIER.equalsIgnoreCase(data.getIdentifier()))
                    .orElse(false);

            return !isGhost;
        }

        return true;
    }
}