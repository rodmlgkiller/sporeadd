package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CamouflagedEvent {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        var camouflagedEffect = effects.CAMOUFLAGED.get();
        boolean hadEffect = player.getPersistentData().getBoolean("wasCamouflaged");

        if (!player.hasEffect(camouflagedEffect) && hadEffect) {
            restorePreviousTeam(player);
            player.getPersistentData().putBoolean("wasCamouflaged", false);

        } else if (player.hasEffect(camouflagedEffect) && !hadEffect) {
            PlayerTeam currentTeam = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (currentTeam != null && !currentTeam.getName().equals("spore")) {
                player.getPersistentData().putString("PreviousTeam", currentTeam.getName());
            }

            player.getPersistentData().putBoolean("wasCamouflaged", true);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var camouflagedEffect = effects.CAMOUFLAGED.get();
        if (!player.hasEffect(camouflagedEffect)) return;

        String previousTeamName = player.getPersistentData().getString("PreviousTeam");
        if (!previousTeamName.isEmpty()) {
            player.getPersistentData().putString("RestoreTeamOnRespawn", previousTeamName);
        }

        restorePreviousTeam(player);
        player.getPersistentData().putBoolean("wasCamouflaged", false);
        player.removeEffect(camouflagedEffect);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;

        var camouflagedEffect = effects.CAMOUFLAGED.get();
        newPlayer.removeEffect(camouflagedEffect);

        String teamToRestore = oldPlayer.getPersistentData().getString("RestoreTeamOnRespawn");
        if (!teamToRestore.isEmpty()) {
            setPlayerTeamSafely(newPlayer, teamToRestore);
        }

        newPlayer.getPersistentData().putBoolean("wasCamouflaged", false);
        newPlayer.getPersistentData().remove("PreviousTeam");
        newPlayer.getPersistentData().remove("RestoreTeamOnRespawn");
    }

    private static void restorePreviousTeam(ServerPlayer player) {
        String previousTeamName = player.getPersistentData().getString("PreviousTeam");
        if (!previousTeamName.isEmpty()) {
            setPlayerTeamSafely(player, previousTeamName);
            player.getPersistentData().remove("PreviousTeam");
        }
    }

    private static void setPlayerTeamSafely(ServerPlayer player, String targetTeamName) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        PlayerTeam targetTeam = scoreboard.getPlayerTeam(targetTeamName);

        if (targetTeam == null) return;
        if (currentTeam != null && currentTeam.getName().equals(targetTeamName)) return;

        if (currentTeam != null) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), currentTeam);
        }

        scoreboard.addPlayerToTeam(player.getScoreboardName(), targetTeam);
    }
}