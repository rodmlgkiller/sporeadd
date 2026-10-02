package com.sporeadds.sporeaddsmod.event;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

@Mod.EventBusSubscriber
public class TeamHandler {

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        Level level = event.getEntity().level();
        if (!level.isClientSide) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                Scoreboard scoreboard = server.getScoreboard();

                // Crear el team "spore" si no existe
                PlayerTeam sporeTeam = scoreboard.getPlayerTeam("spore");
                if (sporeTeam == null) {
                    sporeTeam = scoreboard.addPlayerTeam("spore");
                    sporeTeam.setColor(ChatFormatting.DARK_RED);
                }

                // Crear el team "mercs" si no existe
                PlayerTeam mercsTeam = scoreboard.getPlayerTeam("mercs");
                if (mercsTeam == null) {
                    mercsTeam = scoreboard.addPlayerTeam("mercs");
                    mercsTeam.setColor(ChatFormatting.AQUA);
                }
            }
        }
    }
}
