package com.sporeadds.mixin;

import com.Harbinger.Spore.Sblocks.Acid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Acid.class)
public abstract class SporeAcidMixin {

    // Si tu entorno usa SRG mappings, cambia "entityInside" por "m_7892_"
    @Inject(
            method = "m_7892_", // Nombre SRG
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void ignoreSporeTeamInside(BlockState blockState, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {

        // Verifica que la entidad sea un ser vivo (jugador, mob, etc.)
        if (entity instanceof LivingEntity livingEntity) {

            // Obtiene el equipo al que pertenece la entidad en el mundo (Scoreboard)
            Team team = livingEntity.getTeam();

            // Si la entidad tiene equipo y el nombre del equipo es "spore"
            if (team != null && team.getName().equalsIgnoreCase("spore")) {

                // Cancelamos el método original del Acid de Spore
                // Al hacer esto, el código original nunca se ejecuta, por lo que no le dará Corrosión.
                ci.cancel();
            }
        }
    }
}