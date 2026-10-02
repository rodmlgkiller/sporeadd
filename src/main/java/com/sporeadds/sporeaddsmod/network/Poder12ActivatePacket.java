package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.Poder12;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Poder12ActivatePacket {

    public Poder12ActivatePacket() {}

    public static void encode(Poder12ActivatePacket msg, FriendlyByteBuf buf) {
    }

    public static Poder12ActivatePacket decode(FriendlyByteBuf buf) {
        return new Poder12ActivatePacket();
    }

    public static void handle(Poder12ActivatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                Poder12.activate(sender);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}