package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.network.SyncLevelPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestLevelSyncPacket {

    public RequestLevelSyncPacket() {
    }

    public static void encode(RequestLevelSyncPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestLevelSyncPacket decode(FriendlyByteBuf buf) {
        return new RequestLevelSyncPacket();
    }

    public static void handle(RequestLevelSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SyncLevelPacket.syncLevelToClient(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}