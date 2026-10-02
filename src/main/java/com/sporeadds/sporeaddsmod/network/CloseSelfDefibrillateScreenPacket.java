package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.actionwheel.SelfDefibrillateClientHandler;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class CloseSelfDefibrillateScreenPacket {

    public CloseSelfDefibrillateScreenPacket() {
    }

    public static void encode(CloseSelfDefibrillateScreenPacket msg, net.minecraft.network.FriendlyByteBuf buf) {
    }

    public static CloseSelfDefibrillateScreenPacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new CloseSelfDefibrillateScreenPacket();
    }

    public static void handle(CloseSelfDefibrillateScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(SelfDefibrillateClientHandler::closeScreenIfOpen);
        ctx.get().setPacketHandled(true);
    }
}