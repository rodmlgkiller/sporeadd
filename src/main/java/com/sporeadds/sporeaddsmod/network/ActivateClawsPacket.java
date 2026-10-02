package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** C -> S: el berserker activa la habilidad Claws of Brutality desde el action wheel. */
public class ActivateClawsPacket {

    public ActivateClawsPacket() {
    }

    public static void encode(ActivateClawsPacket msg, FriendlyByteBuf buf) {
    }

    public static ActivateClawsPacket decode(FriendlyByteBuf buf) {
        return new ActivateClawsPacket();
    }

    public static void handle(ActivateClawsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            com.sporeadds.sporeaddsmod.Powers.berserker.ClawsAbility.tryActivate(player);
        });
        context.setPacketHandled(true);
    }
}
