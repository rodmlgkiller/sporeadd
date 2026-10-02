package com.sporeadds.mixin;

import com.Harbinger.Spore.SBlockEntities.OutpostWatcherBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.ArrayList;


@Mixin(value = OutpostWatcherBlockEntity.class, remap = false)
public class OutpostWatcherBlockEntityMixin {

    @Inject(
            method = "checkForPotentialTargets(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onCheckForPotentialTargets(Level level, BlockPos blockPos, CallbackInfo ci) {
        if (level != null) {
            List<Player> players = new ArrayList<>(level.players());
            players.removeIf(player ->
                    player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())
            );
            // Aquí puedes o bien guardar esta lista para usarla en la lógica principal (requiere más hook con Mixin)
            // O mejor, en el predicado Utilities.TARGET_SELECTOR asegúrate que los jugadores del equipo spore devuelven false
        }
    }
}