package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.Powers.AdaptedPhysiologyPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BiomassClimbingMixin {

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void sporeadds$onClimbable(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        BlockState state = player.level().getBlockState(player.blockPosition());

        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()) == null) {
            return;
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

        if (!AdaptedPhysiologyPower.isPhaseableBlock(blockId) || !AdaptedPhysiologyPower.canPhase(player)) {
            return;
        }

        // Si está agachado, NO queremos comportamiento de scaffolding/escalado
        if (player.isShiftKeyDown()) {
            cir.setReturnValue(false);
            return;
        }

        // Si no está agachado, sí permitimos trepar como scaffolding
        cir.setReturnValue(true);
    }
}