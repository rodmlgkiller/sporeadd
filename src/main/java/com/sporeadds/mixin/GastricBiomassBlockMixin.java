package com.sporeadds.mixin;

import com.Harbinger.Spore.Sblocks.GastricBiomassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GastricBiomassBlock.class, remap = false)
public class GastricBiomassBlockMixin {

    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sporeadds$protectTeamSporeFromBileEffects(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo ci) {
        if (player.getTeam() != null && "spore".equals(player.getTeam().getName())) {
            ci.cancel();
        }
    }
}