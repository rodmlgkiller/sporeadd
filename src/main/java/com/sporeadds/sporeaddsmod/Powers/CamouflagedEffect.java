package com.sporeadds.sporeaddsmod.Powers;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

public class CamouflagedEffect extends MobEffect {
    public CamouflagedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA3C1B0);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return;

        PlayerTeam sporeTeam = player.getScoreboard().getPlayerTeam("spore");

        if (sporeTeam != null) {
            // Si NO está en el equipo spore, significa que acaba de recibir el efecto
            // Guardamos su equipo actual antes de cambiarlo
            PlayerTeam currentTeam = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (currentTeam != null && !currentTeam.getName().equals("spore")) {
                player.getPersistentData().putString("PreviousTeam", currentTeam.getName());
            }

            // Cambiar al equipo Spore
            player.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
            player.getScoreboard().addPlayerToTeam(player.getScoreboardName(), sporeTeam);
        }
    }

    // Nota: Aunque usemos onEffectRemoved, a veces Forge no llama a este método si el jugador
    // bebe leche o muere. Por eso también mantenemos la lógica en el TickEvent como respaldo.
    // Pero si se llama, devolvemos el equipo.
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (!(entity instanceof ServerPlayer player)) return;

        String previousTeamName = player.getPersistentData().getString("PreviousTeam");

        if (!previousTeamName.isEmpty()) {
            PlayerTeam previousTeam = player.getScoreboard().getPlayerTeam(previousTeamName);
            if (previousTeam != null) {
                player.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
                player.getScoreboard().addPlayerToTeam(player.getScoreboardName(), previousTeam);
            }
            // Limpiamos el dato
            player.getPersistentData().remove("PreviousTeam");
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}