package com.sporeadds.mixin;

// Cambiamos la importación para apuntar a la clase del monstruo
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.FreezerBlock;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.FreezerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1. Apuntamos a la clase Proto en vez de a la interfaz
// 2. Cambiamos de 'public interface' a 'public abstract class'
@Mixin(value = Proto.class, remap = false)
public abstract class ProtoMixin {

    // Nos inyectamos en su método SpreadFoliageAndConvert
    @Inject(
            method = "SpreadFoliageAndConvert(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onSpreadFoliageAndConvert(Level level, BlockState blockstate, BlockPos blockpos, CallbackInfo ci) {

        if (level.isClientSide) return;

        // Comprobamos si el bloque atacado es tu Freezer
        if (blockstate.getBlock() instanceof FreezerBlock) {

            // Atascamos la máquina
            FreezerBlockEntity.corruptMachine(level, blockpos);

        }
    }
}