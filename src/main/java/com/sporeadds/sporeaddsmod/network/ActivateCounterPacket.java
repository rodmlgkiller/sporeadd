package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C -> S: el berserker activa la habilidad Counter desde el action wheel. */
public class ActivateCounterPacket {

    public ActivateCounterPacket() {
    }

    public static void encode(ActivateCounterPacket msg, FriendlyByteBuf buf) {
    }

    public static ActivateCounterPacket decode(FriendlyByteBuf buf) {
        return new ActivateCounterPacket();
    }

    public static void handle(ActivateCounterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            com.sporeadds.sporeaddsmod.Powers.berserker.CounterAbility.tryActivate(player);
        });
        context.setPacketHandled(true);
    }
}
