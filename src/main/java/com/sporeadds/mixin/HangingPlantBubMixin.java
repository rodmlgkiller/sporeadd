package com.sporeadds.mixin;

import com.Harbinger.Spore.Sblocks.HangingPlantBub;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HangingPlantBub.class, remap = false)
public class HangingPlantBubMixin {

    @Inject(
            method = "m_7892_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void ignoreSporePlayer(BlockState state, Level level, BlockPos blockpos, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player &&
                player.getTeam() != null &&
                "spore".equalsIgnoreCase(player.getTeam().getName())) {
            // Inmunidad total equipo "spore": la planta no reacciona a ellos
            ci.cancel();
        }
    }
}
