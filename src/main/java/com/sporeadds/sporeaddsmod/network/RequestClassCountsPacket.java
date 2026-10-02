package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestClassCountsPacket {

    public static void encode(RequestClassCountsPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestClassCountsPacket decode(FriendlyByteBuf buffer) {
        return new RequestClassCountsPacket();
    }

    public static void handle(RequestClassCountsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                SyncClassCountsPacket.sendTo(player);
            }
        });
        context.setPacketHandled(true);
    }
}
