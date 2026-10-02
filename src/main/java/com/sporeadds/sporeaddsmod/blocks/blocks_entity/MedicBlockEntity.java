package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import com.sporeadds.sporeaddsmod.blocks.medicblock;
import com.sporeadds.sporeaddsmod.client.screen.MedicBlockMenu;
import com.sporeadds.sporeaddsmod.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import com.sporeadds.sporeaddsmod.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger log = LoggerFactory.getLogger(MedicBlockEntity.class);
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    public final AnimationState animationState0 = new AnimationState();
    public final AnimationState animationState1 = new AnimationState();
    public final AnimationState animationState2 = new AnimationState();

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 5000;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public MedicBlockEntity(BlockPos pPos, BlockState pState) {
        super(modblocksentity.MEDIC_BLOCK_ENTITY.get(), pPos, pState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> MedicBlockEntity.this.progress;
                    case 1 -> MedicBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> MedicBlockEntity.this.progress = pValue;
                    case 1 -> MedicBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public boolean isCrafting() {return progress > 0;}

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
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Medical interface");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pcontainerid, Inventory pinventory, Player pplayer) {
        return new MedicBlockMenu(pcontainerid, pinventory, this, this.data);
    }



    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {

        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            getPersistentData().putBoolean("open", true);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            getPersistentData().putBoolean("open", false);
        }

        @Override
        protected void openerCountChanged(Level p_155463_, BlockPos p_155464_, BlockState p_155465_, int p_155466_, int p_155467_) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof MedicBlockMenu;
        }
    };

    @Override
    protected void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        pTag.put("inventory", itemHandler.serializeNBT(registries));
        pTag.putInt("Analize", progress);
        super.saveAdditional(pTag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        itemHandler.deserializeNBT(registries, pTag.getCompound("inventory"));
        progress = pTag.getInt("Analize");
    }

    /*** MÉTODOS DE PROGRESO Y RECETA ***/
    public void tick(Level plevel, BlockPos ppos, BlockState pstate) {
        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(plevel, ppos, pstate);

            if(hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
        data.set(0, progress);
    }

    private boolean hasRecipe() {
        ItemStack input = this.itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack output = this.itemHandler.getStackInSlot(OUTPUT_SLOT);


        boolean hasSyringe = input.getItem() == ModItems.SYRINGE.get()
                && input.hasTag()
                && input.getTag().contains("BloodCode")
                && input.getTag().contains("Filled")
                && input.getTag().getByte("Filled") == 1;


        boolean canInsert = output.isEmpty() ||
                (output.getItem() == ModItems.VACCINE.get() && output.getCount() < output.getMaxStackSize());


        return hasSyringe && canInsert;
    }

    // Aquí está el craft personalizado que copia el NBT y pone el nombre.
    private void craftItem() {
        ItemStack input = this.itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack output = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        ItemStack result = new ItemStack(ModItems.VACCINE.get(), 1);
        if (input.hasTag()) {
            CompoundTag tag = input.getTag().copy();
            result.setTag(tag);

            // Asigna el nombre custom si tiene "BloodSubject"
            if (tag.contains("BloodSubject")) {
                String subject = tag.getString("BloodSubject");
                result.setHoverName(Component.literal(subject + "'s vaccine"));
            }
        }

        if (output.isEmpty()) {
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else if (output.getItem() == ModItems.VACCINE.get() && output.getCount() < output.getMaxStackSize()) {
            output.grow(1);
            output.setTag(result.getTag());
            if (result.hasCustomHoverName()) {
                output.setHoverName(result.getHoverName());
            }
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, output);
        }
    }

    public ContainerOpenersCounter getOpenersCounter() {
        return openersCounter;
    }

    public Direction getFacing() {
        return this.getBlockState().getValue(medicblock.FACING);
    }
}
