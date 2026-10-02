package com.sporeadds.sporeaddsmod.blocks;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.cryoblocke;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import com.sporeadds.sporeaddsmod.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class cryoblock extends BaseEntityBlock {

    public static final com.mojang.serialization.MapCodec<cryoblock> CODEC = simpleCodec(cryoblock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    protected cryoblock(Properties p) {
        super(p);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof cryoblocke cryoEntity) {
                // Recorrer los 27 slots del ItemStackHandler
                for (int i = 0; i < cryoEntity.getInventory().getSlots(); i++) {
                    ItemStack stack = cryoEntity.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
                // Actualiza los vecinos (útil si hay comparadores conectados)
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new cryoblocke(pPos, pState);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof cryoblocke menuProvider) {
                    NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, modblocksentity.CRYO.get(), cryoblocke::tick);
    }
}