package com.sporeadds.sporeaddsmod.spore;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSporeProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerSpore> PLAYER_CAP = CapabilityManager.get(new CapabilityToken<PlayerSpore>() {});

    private PlayerSpore Spore = null;
    private final LazyOptional<PlayerSpore> optional = LazyOptional.of(this::createPlayerSpore);

    private PlayerSpore createPlayerSpore() {
        if(this.Spore == null) {
            this.Spore = new PlayerSpore();
        }

        return this.Spore;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_CAP) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerSpore().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSpore().loadNBTData(nbt);
    }

    public int getSpore() {
        return 0;
    }

    public void addSpore(int i) {
    }
}
