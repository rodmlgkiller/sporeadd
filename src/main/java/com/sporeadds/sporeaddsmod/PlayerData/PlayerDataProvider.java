package com.sporeadds.sporeaddsmod.PlayerData;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.capabilities.*;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDataProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<PlayerData> PLAYER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerData playerData = new PlayerData();
    private final LazyOptional<PlayerData> optional = LazyOptional.of(() -> playerData);

    public static PlayerData get(Player player) {
        return player.getCapability(PLAYER_DATA).orElseThrow(() ->
                new IllegalStateException("Missing PlayerData capability for player " + player.getName().getString()));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_DATA ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        playerData.saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        playerData.loadNBTData(nbt);
    }
}