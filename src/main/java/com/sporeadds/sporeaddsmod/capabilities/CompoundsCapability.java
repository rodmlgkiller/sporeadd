package com.sporeadds.sporeaddsmod.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Inventario portatil de la clase berserker para la habilidad "Compounds": 6 ranuras, una
 * "syringe" por ranura. Persiste en el jugador y se copia a traves de la muerte.
 */
public class CompoundsCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final int SIZE = 6;

    public static final Capability<ICompounds> PLAYER_COMPOUNDS =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final CompoundsImpl backend = new CompoundsImpl();
    private final LazyOptional<ICompounds> instance = LazyOptional.of(() -> backend);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_COMPOUNDS ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.deserializeNBT(nbt);
    }

    public void invalidate() {
        instance.invalidate();
    }

    public interface ICompounds {
        int size();
        ItemStack getStack(int slot);
        void setStack(int slot, ItemStack stack);
        void clearAll();
        void copyFrom(ICompounds other);
        CompoundTag serializeNBT();
        void deserializeNBT(CompoundTag nbt);
    }

    public static class CompoundsImpl implements ICompounds {
        private final ItemStack[] slots = new ItemStack[SIZE];

        public CompoundsImpl() {
            clearAll();
        }

        @Override
        public int size() {
            return SIZE;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
            return slots[slot];
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot < 0 || slot >= SIZE) return;
            slots[slot] = stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public void clearAll() {
            for (int i = 0; i < SIZE; i++) {
                slots[i] = ItemStack.EMPTY;
            }
        }

        @Override
        public void copyFrom(ICompounds other) {
            for (int i = 0; i < SIZE; i++) {
                ItemStack s = other.getStack(i);
                slots[i] = s.isEmpty() ? ItemStack.EMPTY : s.copy();
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            for (int i = 0; i < SIZE; i++) {
                CompoundTag slotTag = new CompoundTag();
                slots[i].save(slotTag);
                nbt.put("compound_" + i, slotTag);
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for (int i = 0; i < SIZE; i++) {
                if (nbt.contains("compound_" + i)) {
                    slots[i] = ItemStack.of(nbt.getCompound("compound_" + i));
                } else {
                    slots[i] = ItemStack.EMPTY;
                }
            }
        }
    }
}
