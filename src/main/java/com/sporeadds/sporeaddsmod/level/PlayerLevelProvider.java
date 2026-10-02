package com.sporeadds.sporeaddsmod.level;

import com.sporeadds.sporeaddsmod.network.SyncLevelPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLevelProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerLevel> PLAYER_LVL = CapabilityManager.get(new CapabilityToken<PlayerLevel>() {});

    private PlayerLevel Level = null;
    private final LazyOptional<PlayerLevel> optional = LazyOptional.of(this::createPlayerLevel);

    private ServerPlayer attachedPlayer = null;

    private PlayerLevel createPlayerLevel() {
        if (this.Level == null) {
            this.Level = new PlayerLevel() {
                @Override
                public void setLevel(int newLevel) {
                    super.setLevel(newLevel);
                    if (attachedPlayer != null) {
                        SyncLevelPacket.syncLevelToClient(attachedPlayer);
                    }
                }

                @Override
                public void addLevel(int value) {
                    super.addLevel(value);
                    if (attachedPlayer != null) {
                        SyncLevelPacket.syncLevelToClient(attachedPlayer);
                    }
                }

                // Sincronizar también cuando cambia el knowledge level
                @Override
                public void setKnowledgeLevel(int newKnowledgeLevel) {
                    super.setKnowledgeLevel(newKnowledgeLevel);
                    if (attachedPlayer != null) {
                        SyncLevelPacket.syncLevelToClient(attachedPlayer);
                    }
                }
            };
        }
        return this.Level;
    }

    public void attachPlayer(ServerPlayer player) {
        this.attachedPlayer = player;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_LVL) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerLevel().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerLevel().loadNBTData(nbt);
    }
}