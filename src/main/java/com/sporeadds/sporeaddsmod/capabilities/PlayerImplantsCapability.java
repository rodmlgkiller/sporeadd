package com.sporeadds.sporeaddsmod.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerImplantsCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<IPlayerImplants> PLAYER_IMPLANTS =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerImplantsImpl backend = new PlayerImplantsImpl();
    private final LazyOptional<IPlayerImplants> instance = LazyOptional.of(() -> backend);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_IMPLANTS ? instance.cast() : LazyOptional.empty();
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

    public IPlayerImplants getBackend() {
        return backend;
    }

    public interface IPlayerImplants {
        ItemStack getImplant(ImplantType type);
        void setImplant(ImplantType type, ItemStack stack);
        void clearAllImplants();
        void copyFrom(IPlayerImplants other);
        CompoundTag serializeNBT();
        void deserializeNBT(CompoundTag nbt);
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
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            for (int i = 0; i < implants.length; i++) {
                CompoundTag implantNBT = new CompoundTag();
                implants[i].save(implantNBT);
                nbt.put("implant_" + i, implantNBT);
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for (int i = 0; i < implants.length; i++) {
                if (nbt.contains("implant_" + i)) {
                    CompoundTag implantNBT = nbt.getCompound("implant_" + i);
                    implants[i] = ItemStack.of(implantNBT);
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