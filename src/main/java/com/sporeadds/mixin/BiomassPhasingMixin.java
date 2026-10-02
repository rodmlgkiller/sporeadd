package com.sporeadds.mixin;

import com.sporeadds.sporeaddsmod.Powers.AdaptedPhysiologyPower;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BiomassPhasingMixin {

    private static final String NO_FALL_TAG = "SporeNoFallTicks";
    private static final int NO_FALL_GRACE_TICKS = 20;

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sporeadds$removeCollision(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(context instanceof EntityCollisionContext entityContext) || !(entityContext.getEntity() instanceof Player player)) {
            return;
        }

        BlockState state = (BlockState) (Object) this;
        if (ForgeRegistries.BLOCKS.getKey(state.getBlock()) == null) {
            return;
        }

        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

        if (AdaptedPhysiologyPower.isPhaseableBlock(blockId) && AdaptedPhysiologyPower.canPhase(player)) {
            double blockTop = pos.getY() + 1.0D;
            boolean isInside = player.getY() < blockTop - 0.01D;

            if (player.isShiftKeyDown() || isInside) {
                CompoundTag data = player.getPersistentData();
                data.putInt(NO_FALL_TAG, NO_FALL_GRACE_TICKS);
                player.fallDistance = 0.0F;
                cir.setReturnValue(Shapes.empty());
            }
        }
    }
}