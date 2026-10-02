package com.sporeadds.sporeaddsmod.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Inventario portatil de la clase berserker para la habilidad "Compounds": 6 ranuras, una
 * "syringe" por ranura. Persiste en el jugador y se copia a traves de la muerte.
 */
public class CompoundsCapability {

    public static final int SIZE = 6;

    public static final Capability<ICompounds> PLAYER_COMPOUNDS = Capability.of(
            "player_compounds",
            holder -> new CompoundsImpl(),
            new Capability.TagSerializer<ICompounds>() {
                @Override
                public CompoundTag save(ICompounds data, HolderLookup.Provider provider) {
                    return data.serializeNBT(provider);
                }

                @Override
                public void load(ICompounds data, CompoundTag tag, HolderLookup.Provider provider) {
                    data.deserializeNBT(provider, tag);
                }
            });

    private CompoundsCapability() {
    }

    public interface ICompounds {
        int size();
        ItemStack getStack(int slot);
        void setStack(int slot, ItemStack stack);
        void clearAll();
        void copyFrom(ICompounds other);
        CompoundTag serializeNBT(HolderLookup.Provider provider);
        void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);
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
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag nbt = new CompoundTag();
            for (int i = 0; i < SIZE; i++) {
                if (!slots[i].isEmpty()) {
                    nbt.put("compound_" + i, slots[i].save(provider));
                }
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            for (int i = 0; i < SIZE; i++) {
                if (nbt.contains("compound_" + i)) {
                    slots[i] = ItemStack.parseOptional(provider, nbt.getCompound("compound_" + i));
                } else {
                    slots[i] = ItemStack.EMPTY;
                }
            }
        }
    }
}
