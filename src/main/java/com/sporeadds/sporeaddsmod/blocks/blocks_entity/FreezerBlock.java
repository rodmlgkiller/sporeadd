package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FreezerBlock extends BaseEntityBlock {

    public static final com.mojang.serialization.MapCodec<FreezerBlock> CODEC = simpleCodec(FreezerBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    public static final BooleanProperty MAIN = BooleanProperty.create("main");
    public static final IntegerProperty CUBE_INDEX = IntegerProperty.create("cube_index", 0, 7);
    public static final VoxelShape SHAPE = Shapes.block();

    public FreezerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(MAIN, false).setValue(CUBE_INDEX, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MAIN, CUBE_INDEX);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Why a freezer cannot be built at a position (see {@link #placementProblem}). */
    public enum PlacementProblem {
        NONE,
        NO_SPACE,
        WATER
    }

    /**
     * Looks at the whole 2x2x2 volume whose lowest corner is {@code origin}: every cell has to be inside the world and
     * replaceable (air, grass, snow layers, ...), and none may hold fluid (the Ultra Freezer cannot stand in water).
     * {@code originIsOurs} skips the corner the freezer block itself already occupies.
     */
    public static PlacementProblem placementProblem(LevelReader level, BlockPos origin, boolean originIsOurs) {
        boolean water = false;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos cell = origin.offset(dx, dy, dz);
                    if (originIsOurs && cell.equals(origin)) continue;
                    if (level.isOutsideBuildHeight(cell) || !level.getBlockState(cell).canBeReplaced()) {
                        return PlacementProblem.NO_SPACE;
                    }
                    if (!level.getFluidState(cell).isEmpty()) {
                        water = true;
                    }
                }
            }
        }
        return water ? PlacementProblem.WATER : PlacementProblem.NONE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;

        // FreezerBlockItem already refuses to place without room for the whole 2x2x2, but a dispenser or another mod can still
        // get here: never leave a lone, half-built freezer behind (it has no model of its own and is not a working machine).
        if (placementProblem(level, pos, true) != PlacementProblem.NONE) {
            level.removeBlock(pos, false);
            if (placer instanceof Player player) {
                if (!player.getAbilities().instabuild) {
                    ItemStack refund = new ItemStack(this);
                    if (!player.getInventory().add(refund)) {
                        player.drop(refund, false);
                    }
                }
            } else {
                Block.popResource(level, pos, new ItemStack(this));
            }
            return;
        }

        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos tgt = pos.offset(dx, dy, dz);
                    int index = dx + dz * 2 + dy * 4;
                    boolean isMain = tgt.equals(pos);
                    level.setBlock(tgt, this.defaultBlockState()
                                    .setValue(MAIN, isMain)
                                    .setValue(CUBE_INDEX, index),
                            3);
                }
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            // Every part knows where it sits in the 2x2x2 (cube_index = dx + dz * 2 + dy * 4), so the origin follows from the part
            // itself. Searching for the main block instead could land on a neighbouring freezer (or find nothing once the main is
            // gone, as happens during this very cascade) and take that one down too.
            int index = state.getValue(CUBE_INDEX);
            BlockPos origin = pos.offset(-(index & 1), -((index >> 2) & 1), -((index >> 1) & 1));
            for (int dx = 0; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = 0; dz <= 1; dz++) {
                        BlockPos tgt = origin.offset(dx, dy, dz);
                        BlockState tgtState = level.getBlockState(tgt);
                        if (tgtState.getBlock() instanceof FreezerBlock
                                && tgtState.getValue(CUBE_INDEX) == dx + dz * 2 + dy * 4) {
                            level.destroyBlock(tgt, true);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(MAIN)) {
            return new FreezerBlockEntity(pos, state);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return state.getValue(MAIN) ? createTickerHelper(type, modblocksentity.FREEZER.get(), FreezerBlockEntity::tick) : null;
    }
}