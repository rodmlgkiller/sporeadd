package com.sporeadds.sporeaddsmod.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PlayerImplantsCapability {

    // Implants are handled by the death cache + respawn flow, so they are not copied on death by the attachment itself.
    public static final Capability<IPlayerImplants> PLAYER_IMPLANTS = Capability.of(
            "player_implants",
            holder -> new PlayerImplantsImpl(),
            new Capability.TagSerializer<IPlayerImplants>() {
                @Override
                public CompoundTag save(IPlayerImplants data, HolderLookup.Provider provider) {
                    return data.serializeNBT(provider);
                }

                @Override
                public void load(IPlayerImplants data, CompoundTag tag, HolderLookup.Provider provider) {
                    data.deserializeNBT(provider, tag);
                }
            },
            false);

    private PlayerImplantsCapability() {
    }

    public interface IPlayerImplants {
        ItemStack getImplant(ImplantType type);
        void setImplant(ImplantType type, ItemStack stack);
        void clearAllImplants();
        void copyFrom(IPlayerImplants other);
        CompoundTag serializeNBT(HolderLookup.Provider provider);
        void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);
    }

    public static class PlayerImplantsImpl implements IPlayerImplants {
        private final ItemStack[] implants = new ItemStack[6];

        public PlayerImplantsImpl() {
            clearAllImplants();
        }

        @Override
        public ItemStack getImplant(ImplantType type) {
            return implants[type.ordinal()].copy();
        }

        @Override
        public void setImplant(ImplantType type, ItemStack stack) {
            implants[type.ordinal()] = stack.copy();
        }

        @Override
        public void clearAllImplants() {
            for (int i = 0; i < implants.length; i++) {
                implants[i] = ItemStack.EMPTY;
            }
        }

        @Override
        public void copyFrom(IPlayerImplants other) {
            for (ImplantType type : ImplantType.values()) {
                setImplant(type, other.getImplant(type));
            }
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag nbt = new CompoundTag();
            for (int i = 0; i < implants.length; i++) {
                if (!implants[i].isEmpty()) {
                    nbt.put("implant_" + i, implants[i].save(provider));
                }
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            for (int i = 0; i < implants.length; i++) {
                if (nbt.contains("implant_" + i)) {
                    implants[i] = ItemStack.parseOptional(provider, nbt.getCompound("implant_" + i));
                } else {
                    implants[i] = ItemStack.EMPTY;
                }
            }
        }
    }

    public enum ImplantType {
        EYE(0),
        TORSO(1),
        RIGHT_ARM(2),
        LEFT_ARM(3),
        RIGHT_LEG(4),
        LEFT_LEG(5);

        private final int index;

        ImplantType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
