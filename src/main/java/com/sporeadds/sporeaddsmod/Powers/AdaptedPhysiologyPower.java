package com.sporeadds.sporeaddsmod.Powers;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import java.util.HashSet;
import java.util.Set;

public class AdaptedPhysiologyPower {

    private static final Set<String> PHASEABLE_BLOCKS = new HashSet<>();

    static {
        PHASEABLE_BLOCKS.add("spore:membrane_block");
        PHASEABLE_BLOCKS.add("spore:rooted_biomass");
        PHASEABLE_BLOCKS.add("spore:biomass_block");
        PHASEABLE_BLOCKS.add("spore:sicken_biomass_block");
        PHASEABLE_BLOCKS.add("spore:gastric_biomass_block");
        PHASEABLE_BLOCKS.add("spore:calcified_biomass_block");
    }

    public static boolean isPhaseableBlock(String blockId) {
        return PHASEABLE_BLOCKS.contains(blockId);
    }

    public static boolean canPhase(Player player) {
        if (player.isSpectator()) return true;

        boolean hasCamouflage = player.getActiveEffects().stream()
                .anyMatch(instance -> instance.getEffect() instanceof CamouflagedEffect);
        if (hasCamouflage) return false;

        Team team = player.getTeam();
        if (team == null || !team.getName().equals("spore")) {
            return false;
        }

        return SporeAddsConfig.SPORE_PLAYERS_CAN_PHASE_BIOMASS.get();
    }
}