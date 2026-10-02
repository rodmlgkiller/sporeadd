package com.sporeadds.sporeaddsmod.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import com.sporeadds.sporeaddsmod.effects.SelfDefibrillateAbility;

import java.util.function.Supplier;

public class RequestSelfDefibrillateStartPacket {

    public RequestSelfDefibrillateStartPacket() {
    }

    public static void encode(RequestSelfDefibrillateStartPacket msg, net.minecraft.network.FriendlyByteBuf buf) {
    }

    public static RequestSelfDefibrillateStartPacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new RequestSelfDefibrillateStartPacket();
    }

    public static void handle(RequestSelfDefibrillateStartPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) {
            ctx.get().setPacketHandled(true);
            return;
        }

        ctx.get().enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.effects.SelfDefibrillateAbility.tryStart(player);
        });
        ctx.get().setPacketHandled(true);
    }
}