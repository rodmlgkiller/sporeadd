package com.sporeadds.sporeaddsmod.network;

import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSelfDefibrillateScreenPacket {

    public OpenSelfDefibrillateScreenPacket() {
    }

    public static void encode(OpenSelfDefibrillateScreenPacket msg, net.minecraft.network.FriendlyByteBuf buf) {
    }

    public static OpenSelfDefibrillateScreenPacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new OpenSelfDefibrillateScreenPacket();
    }

    public static void handle(OpenSelfDefibrillateScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.actionwheel.SelfDefibrillateClientHandler.openScreen();
        });
        ctx.get().setPacketHandled(true);
    }
}