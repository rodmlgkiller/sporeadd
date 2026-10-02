package com.sporeadds.sporeaddsmod.PlayerData;

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

public class SporeIdentifierProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<SporeIdentifierData> SPORE_IDENTIFIER = CapabilityManager.get(new CapabilityToken<SporeIdentifierData>() { });

    private SporeIdentifierData backend = null;
    private final LazyOptional<SporeIdentifierData> optional = LazyOptional.of(this::createBackend);

    private SporeIdentifierData createBackend() {
        if (this.backend == null) {
            this.backend = new SporeIdentifierData();
        }
        return this.backend;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SPORE_IDENTIFIER) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createBackend().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createBackend().loadNBTData(nbt);
    }
}