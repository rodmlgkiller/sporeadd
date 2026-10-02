package com.sporeadds.mixin;

import com.Harbinger.Spore.Sblocks.SickenBiomassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SickenBiomassBlock.class)
public class SickenBiomassBlockMixin {

    // attack es el método "attack" (cuando se golpea el bloque)
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$protectTeamSporeFromWither(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo ci) {

        // Comprobamos si el jugador está en la familia/team "spore"
        if (player.getTeam() != null && player.getTeam().getName().equals("spore")) {

            // Cancelamos la inyección del efecto de Wither.
            // Esto anulará el código de SickenBiomassBlock para este jugador.
            // Como el método attack base de Minecraft es vacío, cancelarlo es 100% seguro y no te impedirá minarlo.
            ci.cancel();
        }
    }
}