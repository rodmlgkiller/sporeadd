package com.sporeadds.sporeaddsmod.blocks.blocks_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import com.sporeadds.sporeaddsmod.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sporeadds.sporeaddsmod.client.screen.cryomenu;

public class cryoblocke extends BlockEntity implements MenuProvider {

    // 1. Inventario de 27 slots (como un cofre)
    private final ItemStackHandler itemHandler = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ItemStackHandler getInventory() {
        return this.itemHandler;
    }

    public cryoblocke(BlockPos pos, BlockState state) {
        super(modblocksentity.CRYO.get(), pos, state);
    }

    // 2. Lógica de absorción y conversión (Ticking)
    public static void tick(Level level, BlockPos pos, BlockState state, cryoblocke entity) {
        if (level.isClientSide) return;

        // Intentar absorber cada 20 ticks (1 segundo)
        if (level.getGameTime() % 20 == 0) {
            BlockPos belowPos = pos.below();
            if (level.getFluidState(belowPos).is(Fluids.WATER) && level.getFluidState(belowPos).isSource()) {

                // Quitamos el agua
                level.setBlockAndUpdate(belowPos, Blocks.AIR.defaultBlockState());

                // Generamos loot con probabilidades
                entity.generateLoot();
            }
        }
    }

    private void generateLoot() {
        double chance = Math.random();
        ItemStack stack = ItemStack.EMPTY;
        if (chance < 0.65) stack = new ItemStack(Items.SNOWBALL);
        else if (chance < 0.80) stack = new ItemStack(Items.SNOW);
        else if (chance < 0.90) stack = new ItemStack(Items.SNOW_BLOCK);
        else if (chance < 0.9775) stack = new ItemStack(Items.ICE);
        else if (chance < 0.9975) stack = new ItemStack(Items.PACKED_ICE);
        else stack = new ItemStack(Items.BLUE_ICE);

        if (!stack.isEmpty()) {
            // Insertamos en el inventario propio
            ItemHandlerHelper.insertItemStacked(this.itemHandler, stack, false);
            // Sincronizamos
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 3. Soporte para Tolvas (Hoppers) y Menú
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

    // 4. Guardado de datos (NBT)
    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        nbt.put("inventory", itemHandler.serializeNBT(registries));
        super.saveAdditional(nbt, registries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        itemHandler.deserializeNBT(registries, nbt.getCompound("inventory"));
    }

    // 5. Interfaz (MenuProvider)
    @Override
    public Component getDisplayName() {
        return Component.literal("Cryo Absorber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new cryomenu(id, inv, this);
    }

    // Sincronización de paquetes para que el cliente vea los cambios
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}