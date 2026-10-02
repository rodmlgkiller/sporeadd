package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.client.screen.ScientistMenu;
import com.sporeadds.sporeaddsmod.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScientistBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                // Slot azul acepta spore:frozen_decayed_biomass
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                return ResourceLocation.fromNamespaceAndPath("spore", "frozen_decayed_biomass").equals(id);
            }
            if (slot == 1) {
                return stack.getItem() == Items.PAPER;
            }
            if (slot == 2) {
                return false; // output
            }
            return super.isItemValid(slot, stack);
        }

    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // Variables para la biomasa
    private int storedBiomass = 0;
    private int processingProgress = 0;
    private static final int MAX_PROGRESS = 100;

    protected final ContainerData data;

    public ScientistBlockEntity(BlockPos pos, BlockState state) {
        super(modblocksentity.SCIENTIST_BLOCK_ENTITY.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> ScientistBlockEntity.this.storedBiomass;
                    case 1 -> ScientistBlockEntity.this.processingProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> ScientistBlockEntity.this.storedBiomass = value;
                    case 1 -> ScientistBlockEntity.this.processingProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
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

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sporeadd.scientist_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ScientistMenu(containerId, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("stored_biomass", storedBiomass);
        tag.putInt("processing_progress", processingProgress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        storedBiomass = tag.getInt("stored_biomass");
        processingProgress = tag.getInt("processing_progress");
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        // Procesar biomasa del slot azul
        ItemStack biomasStack = itemHandler.getStackInSlot(0);
        if (!biomasStack.isEmpty() && processingProgress < MAX_PROGRESS && storedBiomass <=149) {
            processingProgress++;
            setChanged();

            // Cuando termine el procesamiento
            if (processingProgress >= MAX_PROGRESS) {
                storedBiomass += 1;
                biomasStack.shrink(1);
                processingProgress = 0;
                setChanged();
            }
        }
    }

    // Método para crear research items
    public boolean createResearchItem(int researchLevel) {
        int cost = researchLevel; // 50, 100, 150
        ItemStack paperStack = itemHandler.getStackInSlot(1);
        ItemStack outputStack = itemHandler.getStackInSlot(2);

        // Verificar requisitos
        if (storedBiomass < cost || paperStack.isEmpty() || !outputStack.isEmpty()) {
            return false;
        }

        // Determinar que item crear
        ItemStack result = ItemStack.EMPTY;
        switch (researchLevel) {
            case 50 -> result = new ItemStack(ModItems.RESEARCH_50.get());
            case 100 -> result = new ItemStack(ModItems.RESEARCH_100.get());
            case 150 -> result = new ItemStack(ModItems.RESEARCH_150.get());
        }

        if (!result.isEmpty()) {
            // Gastar recursos
            storedBiomass -= cost;
            paperStack.shrink(1);

            // Colocar resultado
            itemHandler.setStackInSlot(2, result);
            setChanged();
            return true;
        }
        return false;
    }

    public int getStoredBiomass() {
        return storedBiomass;
    }

    public int getProcessingProgress() {
        return processingProgress;
    }
}
