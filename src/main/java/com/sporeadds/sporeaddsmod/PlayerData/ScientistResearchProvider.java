package com.sporeadds.sporeaddsmod.PlayerData;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ScientistResearchProvider implements ICapabilitySerializable<Tag> {

    public static final Capability<ScientistResearchData> SCIENTIST_RESEARCH =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final ScientistResearchData data = new ScientistResearchData();
    private final LazyOptional<ScientistResearchData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == SCIENTIST_RESEARCH ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        data.saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            data.loadNBTData(tag);
        }
    }
}