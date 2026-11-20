package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: "send me my ability toggles". Sent when the ability selector opens so the GUI always shows
 * what the server really has, whatever happened before (death, dimension change, another mod moving the player...).
 */
public class RequestSwitchSyncPacket {

    public RequestSwitchSyncPacket() {
    }

    public static void encode(RequestSwitchSyncPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestSwitchSyncPacket decode(FriendlyByteBuf buf) {
        return new RequestSwitchSyncPacket();
    }

    public static void handle(RequestSwitchSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerToData.syncTo(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
