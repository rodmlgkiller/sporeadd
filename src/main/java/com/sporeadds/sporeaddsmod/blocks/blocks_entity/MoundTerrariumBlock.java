package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import com.sporeadds.sporeaddsmod.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class MoundTerrariumBlock extends BaseEntityBlock {

    public static final BooleanProperty HAS_MOUND = BooleanProperty.create("has_mound");
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    private static final VoxelShape SHAPE = Shapes.or(
            box(1, 0, 0, 15, 1, 16),
            box(0, 0, 1, 1, 1, 15),
            box(15, 0, 1, 16, 1, 15),
            box(2, 1, 1, 14, 15, 2),
            box(2, 1, 14, 14, 15, 15),
            box(1, 1, 2, 2, 15, 14),
            box(14, 1, 2, 15, 15, 14),
            box(2, 15, 2, 14, 16, 14)
    );

    public MoundTerrariumBlock(Properties properties) {
        super(properties
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .mapColor(MapColor.PLANT)
                .noOcclusion());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HAS_MOUND, false)
                .setValue(LINKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_MOUND, LINKED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);
        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(heldStack.getItem());

        if (!level.isClientSide && state.getValue(HAS_MOUND)) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MoundTerrariumBlockEntity terrarium) {

                if (heldId != null && heldId.toString().equals("spore:reaver") && terrarium.getHp() >= 15) {
                    doReaverHarvest((ServerLevel) level, pos, player, heldStack, terrarium);
                    return InteractionResult.CONSUME;
                }

                if (heldStack.is(Items.SHEARS) && terrarium.getHp() >= 1) {
                    doShearHarvest((ServerLevel) level, pos, player, heldStack, terrarium);
                    return InteractionResult.CONSUME;
                }

                NetworkHooks.openScreen((ServerPlayer) player, terrarium, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void doReaverHarvest(ServerLevel level, BlockPos pos, Player player, ItemStack tool, MoundTerrariumBlockEntity terrarium) {
        RandomSource random = level.random;

        ResourceLocation reaveSoundId = ResourceLocation.fromNamespaceAndPath("spore", "reaver_reave");
        var reaveSound = BuiltInRegistries.SOUND_EVENT.get(reaveSoundId);
        if (reaveSound != null) {
            level.playSound(null, pos, reaveSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        ResourceLocation damageSoundId = ResourceLocation.fromNamespaceAndPath("spore", "organoid_damage");
        var damageSound = BuiltInRegistries.SOUND_EVENT.get(damageSoundId);
        if (damageSound != null) {
            level.playSound(null, pos, damageSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        ResourceLocation bloodParticleId = ResourceLocation.fromNamespaceAndPath("spore", "blood_particle");
        var bloodParticle = BuiltInRegistries.PARTICLE_TYPE.get(bloodParticleId);
        if (bloodParticle instanceof net.minecraft.core.particles.ParticleOptions options) {
            level.sendParticles(
                    options,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    20, 0.35D, 0.35D, 0.35D, 0.02D
            );
        }

        Block biomassBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "biomass_block"));
        if (biomassBlock != null) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, biomassBlock.defaultBlockState()),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    28, 0.30D, 0.30D, 0.30D, 0.04D
            );
        }

        spawnVisualSplitExperience(level, Vec3.atCenterOf(pos), 25, 5);

        int looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, tool);

        Item mutatedFiber = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "mutated_fiber"));
        if (mutatedFiber != null) {
            int count = 1 + random.nextInt(5) + looting;
            Containers.dropItemStack(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    new ItemStack(mutatedFiber, count)
            );
        }

        Item tumor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "tumor"));
        if (tumor != null) {
            int count = 1 + random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
            Containers.dropItemStack(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    new ItemStack(tumor, count)
            );
        }

        Item organoidMembrane = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "organoid_membrane"));
        if (organoidMembrane != null) {
            double chance = Math.min(1.0D, 0.80D + (0.05D * looting));
            if (random.nextDouble() < chance) {
                int count = 1 + random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        new ItemStack(organoidMembrane, count)
                );
            }
        }

        terrarium.setHp(0);
        terrarium.setStomach(0);
        player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    private void spawnVisualSplitExperience(ServerLevel level, Vec3 center, int totalXp, int orbCount) {
        if (totalXp <= 0 || orbCount <= 0) {
            return;
        }

        int clampedOrbCount = Math.min(orbCount, totalXp);
        int baseValue = totalXp / clampedOrbCount;
        int remainder = totalXp % clampedOrbCount;

        for (int i = 0; i < clampedOrbCount; i++) {
            int value = baseValue + (i < remainder ? 1 : 0);

            double angle = (Math.PI * 2D * i) / clampedOrbCount;
            double radius = 0.18D + (level.random.nextDouble() * 0.18D);

            double x = center.x + Math.cos(angle) * radius;
            double y = center.y + 0.15D + (level.random.nextDouble() * 0.2D);
            double z = center.z + Math.sin(angle) * radius;

            ExperienceOrb orb = new ExperienceOrb(level, x, y, z, value);
            orb.setDeltaMovement(
                    (level.random.nextDouble() - 0.5D) * 0.06D,
                    0.03D + level.random.nextDouble() * 0.05D,
                    (level.random.nextDouble() - 0.5D) * 0.06D
            );
            level.addFreshEntity(orb);
        }
    }

    private void doShearHarvest(ServerLevel level, BlockPos pos, Player player, ItemStack tool, MoundTerrariumBlockEntity terrarium) {
        RandomSource random = level.random;

        level.playSound(null, pos, SoundEvents.WET_GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

        Block growthsSmallBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "growths_small"));
        if (growthsSmallBlock != null) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, growthsSmallBlock.defaultBlockState()),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    16, 0.30D, 0.30D, 0.30D, 0.02D
            );
        }

        spawnVisualSplitExperience(level, Vec3.atCenterOf(pos), 5, 3);

        Item bloomfung2 = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "bloomfung2"));
        if (bloomfung2 != null && random.nextDouble() < 0.40D) {
            int count = 1 + random.nextInt(2);
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, new ItemStack(bloomfung2, count));
        }

        Item bloomfung = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "blomfung"));
        if (bloomfung != null && random.nextDouble() < 0.70D) {
            int count = 2 + random.nextInt(3);
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, new ItemStack(bloomfung, count));
        }

        Item biomassBulb = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", "biomass_bulb"));
        if (biomassBulb != null && random.nextDouble() < 0.40D) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, new ItemStack(biomassBulb, 1));
        }

        String[] randomPlantDrops = new String[] {
                "growths_big",
                "fungal_roots",
                "growth_mycelium",
                "mycelium_veins",
                "growths_small",
                "fungal_stem_sapling",
        };

        int chosenCount = 2 + random.nextInt(4);
        for (int i = 0; i < chosenCount; i++) {
            String id = randomPlantDrops[random.nextInt(randomPlantDrops.length)];
            Item dropItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("spore", id));
            if (dropItem != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, new ItemStack(dropItem, 1));
            }
        }

        terrarium.setHp(Mth.clamp(terrarium.getHp() - 1, 0, 15));
        player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(this);

        boolean hasMound = state.getValue(HAS_MOUND);
        boolean linked = state.getValue(LINKED);

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("HasMound", hasMound);
        tag.putBoolean("Linked", linked);
        if (hasMound) {
            tag.putInt("CustomModelData", 1);
        }

        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(HAS_MOUND, false)
                .setValue(LINKED, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        boolean hasMound = tag.getBoolean("HasMound");
        boolean linked = hasMound && tag.getBoolean("Linked");

        BlockState newState = level.getBlockState(pos);
        if (newState.hasProperty(HAS_MOUND)) {
            newState = newState.setValue(HAS_MOUND, hasMound);
        }
        if (newState.hasProperty(LINKED)) {
            newState = newState.setValue(LINKED, linked);
        }

        if (newState != state) {
            level.setBlock(pos, newState, 3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof MoundTerrariumBlockEntity terrarium) {
                terrarium.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        }
                    }
                });

                if (!level.isClientSide && state.getValue(HAS_MOUND)) {
                    spawnMoundFromTerrarium(level, pos, terrarium.getHp() < 15);
                }
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void spawnMoundFromTerrarium(Level level, BlockPos pos, boolean linked) {
        ResourceLocation moundId = ResourceLocation.fromNamespaceAndPath("spore", "mound");
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(moundId);

        if (entityType == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = entityType.create(serverLevel);
        if (entity == null) {
            return;
        }

        entity.moveTo(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );

        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        tag.putBoolean("linked", linked);
        tag.putInt("age", 1);
        entity.load(tag);

        entity.getPersistentData().putBoolean("SporeAdds_NoHardFloorDespawn", true);
        entity.addTag("anti_reaver");

        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }

        level.addFreshEntity(entity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoundTerrariumBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, modblocksentity.MOUND_TERRARIUM_ENTITY.get(), MoundTerrariumBlockEntity::clientTick)
                : createTickerHelper(type, modblocksentity.MOUND_TERRARIUM_ENTITY.get(), MoundTerrariumBlockEntity::serverTick);
    }
}