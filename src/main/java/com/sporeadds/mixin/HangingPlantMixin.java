package com.sporeadds.mixin;

import com.Harbinger.Spore.Sblocks.HangingPlant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = HangingPlant.class, remap = false)
public class HangingPlantMixin {
    @Inject(
            method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void ignoreSporeTeam(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player &&
                player.getTeam() != null &&
                "spore".equalsIgnoreCase(player.getTeam().getName())) {
            ci.cancel();
        }
    }
}

