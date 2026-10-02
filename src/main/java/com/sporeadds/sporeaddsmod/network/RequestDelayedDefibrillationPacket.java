package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.abilities.DelayedDefibrillationAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestDelayedDefibrillationPacket {

    public RequestDelayedDefibrillationPacket() {
    }

    public RequestDelayedDefibrillationPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(RequestDelayedDefibrillationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DelayedDefibrillationAbility.tryActivate(player);
            }
        });
        context.setPacketHandled(true);
    }
}