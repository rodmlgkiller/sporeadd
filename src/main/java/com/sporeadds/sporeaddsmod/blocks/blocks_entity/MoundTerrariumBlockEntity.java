package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.client.gui.MoundTerrariumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoundTerrariumBlockEntity extends BlockEntity implements MenuProvider {

    private int animationTick;
    private int hp = 0;
    private int stomach = 0;
    private int scent = 0;

    private int digestionTimer = 0;
    private int digestionProgress = 0;
    private boolean isDigesting = false;
    private int scentTimer = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MoundTerrariumBlockEntity.this.hp;
                case 1 -> MoundTerrariumBlockEntity.this.stomach;
                case 2 -> MoundTerrariumBlockEntity.this.scent;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> MoundTerrariumBlockEntity.this.hp = value;
                case 1 -> MoundTerrariumBlockEntity.this.stomach = value;
                case 2 -> MoundTerrariumBlockEntity.this.scent = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public MoundTerrariumBlockEntity(BlockPos pos, BlockState state) {
        super(modblocksentity.MOUND_TERRARIUM_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MoundTerrariumBlockEntity be) {
        boolean changed = false;

        if (!be.isDigesting && be.hp < 15) {
            ItemStack biomassStack = be.itemHandler.getStackInSlot(0);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(biomassStack.getItem());

            if (itemId != null && itemId.toString().equals("spore:biomass")) {
                be.itemHandler.extractItem(0, 1, false);
                be.isDigesting = true;
                be.digestionProgress = 0;
                be.digestionTimer = 0;
                changed = true;
            }
        }

        if (be.isDigesting) {
            be.digestionTimer++;
            if (be.digestionTimer >= 20) { // 1 segundo
                be.digestionTimer = 0;
                be.digestionProgress++;
                be.stomach++;
                changed = true;

                if (be.stomach >= 36) {
                    be.stomach = 0;
                    if (be.hp < 15) {
                        be.hp++;
                    }
                }

                if (be.digestionProgress >= 20) { // 20 segundos total por biomasa
                    be.isDigesting = false;
                }
            }
        }

        if (be.hp > 14) {
            be.scentTimer++;
            if (be.scentTimer >= 240) {
                be.scentTimer = 0;
                if (be.scent < 25) {
                    be.scent++;
                    changed = true;
                }
            }
        } else {
            be.scentTimer = 0;
        }

        if (be.scent >= 25) {
            ItemStack bottleStack = be.itemHandler.getStackInSlot(1);
            ItemStack resultStack = be.itemHandler.getStackInSlot(2);

            if (bottleStack.is(Items.GLASS_BOTTLE)) {
                ResourceLocation spawnEggId = ResourceLocation.fromNamespaceAndPath("spore", "scent_spawnegg");
                var spawnEggItem = BuiltInRegistries.ITEM.get(spawnEggId);

                if (spawnEggItem != null && (resultStack.isEmpty() || (resultStack.is(spawnEggItem) && resultStack.getCount() < 64))) {
                    be.itemHandler.extractItem(1, 1, false);

                    if (resultStack.isEmpty()) {
                        be.itemHandler.setStackInSlot(2, new ItemStack(spawnEggItem, 1));
                    } else {
                        resultStack.grow(1);
                    }

                    be.scent = 0;
                    changed = true;

                    if (level instanceof ServerLevel serverLevel) {
                        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("spore", "puff");
                        var puffEvent = BuiltInRegistries.SOUND_EVENT.get(soundId);
                        if (puffEvent != null) {
                            serverLevel.playSound(null, pos, puffEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }

                        serverLevel.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                        ResourceLocation particleId = ResourceLocation.fromNamespaceAndPath("spore", "spore_particle");
                        var particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleId);
                        if (particleType instanceof ParticleOptions options) {
                            int count = 3 + level.random.nextInt(5);
                            double x = pos.getX() + 0.5D;
                            double y = pos.getY() + 0.5D;
                            double z = pos.getZ() + 0.5D;
                            serverLevel.sendParticles(options, x, y, z, count, 0.2D, 0.2D, 0.2D, 0.0D);
                        }
                    }
                }
            }
        }

        be.updateLinkedBlockState();

        if (changed) {
            be.setChanged();
            be.sync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MoundTerrariumBlockEntity blockEntity) {
        blockEntity.animationTick++;
    }

    public int getAnimationTick() {
        return animationTick;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = clamp(hp, 0, 15);
        updateLinkedBlockState();
        setChanged();
        sync();
    }

    public int getStomach() {
        return stomach;
    }

    public void setStomach(int stomach) {
        this.stomach = clamp(stomach, 0, 36);
        setChanged();
        sync();
    }

    public int getScent() {
        return scent;
    }

    public void setScent(int scent) {
        this.scent = clamp(scent, 0, 25);
        setChanged();
        sync();
    }

    private void updateLinkedBlockState() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        if (!state.hasProperty(MoundTerrariumBlock.HAS_MOUND) || !state.hasProperty(MoundTerrariumBlock.LINKED)) {
            return;
        }

        if (!state.getValue(MoundTerrariumBlock.HAS_MOUND)) {
            if (state.getValue(MoundTerrariumBlock.LINKED)) {
                level.setBlock(worldPosition, state.setValue(MoundTerrariumBlock.LINKED, false), 3);
            }
            return;
        }

        boolean shouldBeLinked = this.hp < 15;
        if (state.getValue(MoundTerrariumBlock.LINKED) != shouldBeLinked) {
            level.setBlock(worldPosition, state.setValue(MoundTerrariumBlock.LINKED, shouldBeLinked), 3);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sporeadds.mound_terrarium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MoundTerrariumMenu(id, playerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("animation_tick", animationTick);
        tag.putInt("hp", hp);
        tag.putInt("stomach", stomach);
        tag.putInt("scent", scent);
        tag.putInt("digestion_timer", digestionTimer);
        tag.putInt("digestion_progress", digestionProgress);
        tag.putBoolean("is_digesting", isDigesting);
        tag.putInt("scent_timer", scentTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }

        animationTick = tag.getInt("animation_tick");
        hp = clamp(tag.getInt("hp"), 0, 15);
        stomach = clamp(tag.getInt("stomach"), 0, 36);
        scent = clamp(tag.getInt("scent"), 0, 25);
        digestionTimer = tag.getInt("digestion_timer");
        digestionProgress = tag.getInt("digestion_progress");
        isDigesting = tag.getBoolean("is_digesting");
        scentTimer = tag.getInt("scent_timer");

        updateLinkedBlockState();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}