package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.spore.PlayerSpore;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncMoundCountPacket {
    private final CompoundTag nbt;

    public SyncMoundCountPacket(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static void encode(SyncMoundCountPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.nbt);
    }

    public static SyncMoundCountPacket decode(FriendlyByteBuf buf) {
        return new SyncMoundCountPacket(buf.readNbt());
    }

    public static void handle(SyncMoundCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.syncMoundCount(msg.nbt)));
        ctx.get().setPacketHandled(true);
    }
}