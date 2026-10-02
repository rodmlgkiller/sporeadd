package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Powers.AdaptedPhysiologyPower;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerOverlayMixin {

    // 1. Quitar el daño por asfixia (isInvulnerableTo es el método más seguro)
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void sporeadds$preventSuffocation(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (source.is(DamageTypes.IN_WALL)) {
            Player player = (Player) (Object) this;
            BlockPos eyePos = BlockPos.containing(player.getEyePosition());
            BlockState state = player.level().getBlockState(eyePos);
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

            if (AdaptedPhysiologyPower.isPhaseableBlock(blockId) && AdaptedPhysiologyPower.canPhase(player)) {
                cir.setReturnValue(true); // Hace al jugador invulnerable a este daño
            }
        }
    }
}