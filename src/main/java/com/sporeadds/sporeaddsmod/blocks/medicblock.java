package com.sporeadds.sporeaddsmod.blocks;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class medicblock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0,0,0,16,16,16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected medicblock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter player, BlockPos pos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientBlockExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, net.minecraft.client.particle.ParticleEngine manager) {
                manager.destroy(pos, state);
                return true;
            }
        });
    }

    @Override
    public void onRemove(BlockState pstate, Level plevel, BlockPos ppos, BlockState pnewstate, boolean pismoving) {
        if (pstate.getBlock() != pnewstate.getBlock()) {
            BlockEntity blockEntity = plevel.getBlockEntity(ppos);
            if (blockEntity instanceof MedicBlockEntity) {
                ((MedicBlockEntity) blockEntity).drops();
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState pstate, Level plevel, BlockPos ppos, Player player, InteractionHand phand, BlockHitResult phit) {
        if (!plevel.isClientSide()) {
            BlockEntity entity = plevel.getBlockEntity(ppos);
            if(entity instanceof MedicBlockEntity) {
                NetworkHooks.openScreen(((ServerPlayer) player), (MedicBlockEntity) entity, ppos);
            } else {
                throw new IllegalStateException("missing");
            }
        }
        return InteractionResult.sidedSuccess(plevel.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MedicBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level plevel, BlockState pstate, BlockEntityType<T> pBlockEntityType) {
        if(plevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, modblocksentity.MEDIC_BLOCK_ENTITY.get(),
                (plevel1, ppos, pstate1, pblock) -> pblock.tick(plevel, ppos, pstate));
    }
}
