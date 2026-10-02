package com.sporeadds.sporeaddsmod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class SporeFactionHelper {

    // Método mejorado y más resistente para comprobar si el jugador está en el equipo spore
    public static boolean isSporePlayer(LivingEntity entity) {
        if (entity instanceof Player player) {

            // Usamos el Scoreboard del mundo para asegurarnos de que la lectura sea precisa y real en el servidor.
            // (La IA de los mobs siempre corre en el servidor)
            if (!player.level().isClientSide()) {
                Team team = player.level().getScoreboard().getPlayersTeam(player.getScoreboardName());
                if (team != null && team.getName().equals("spore")) {
                    return true;
                }
            } else {
                // Caída (fallback) para si se consulta desde el cliente (animaciones/renderizado visual)
                Team team = player.getTeam();
                if (team != null && team.getName().equals("spore")) {
                    return true;
                }
            }
        }
        return false;
    }

    // Comprueba si el ID del Mob está en la lista de la configuración
    public static boolean isEntityInList(Mob mob, List<? extends String> list) {
        if (list == null || list.isEmpty()) return false;

        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        return key != null && list.contains(key.toString());
    }
}