package com.sporeadds.sporeaddsmod.blocks;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockCrafterEntity;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.sporeadds.sporeaddsmod.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MedicBlockCrafter extends BaseEntityBlock {

    public static final com.mojang.serialization.MapCodec<MedicBlockCrafter> CODEC = simpleCodec(MedicBlockCrafter::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    public static final VoxelShape SHAPE = Block.box(0,0,0,16,16,16);

    public MedicBlockCrafter(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MedicBlockCrafterEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, modblocksentity.MEDIC_BLOCK_CONSTRUCTOR_ENTITY.get(),
                (lvl, pos, st, be) -> MedicBlockCrafterEntity.tick(lvl, pos, st, (MedicBlockCrafterEntity) be));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof MedicBlockCrafterEntity entity)) {
                throw new IllegalStateException("BlockEntity no es MedicBlockCrafterEntity en " + pos);
            }
            NetworkHooks.openScreen((ServerPlayer) player, entity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
