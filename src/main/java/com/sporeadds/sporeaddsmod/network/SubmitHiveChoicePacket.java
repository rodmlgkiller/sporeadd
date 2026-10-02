package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C -> S. El jugador ha elegido en la pantalla de la colmena.
 *  - surrender = true  -> "give up" rojo oscuro (rendición)
 *  - surrender = false -> "give up" normal (perecer). También es lo que se envía si expira el timer.
 */
public class SubmitHiveChoicePacket {

    private final boolean surrender;

    public SubmitHiveChoicePacket(boolean surrender) {
        this.surrender = surrender;
    }

    public static void encode(SubmitHiveChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.surrender);
    }

    public static SubmitHiveChoicePacket decode(FriendlyByteBuf buf) {
        return new SubmitHiveChoicePacket(buf.readBoolean());
    }

    public static void handle(SubmitHiveChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        boolean surrender = msg.surrender;
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            com.sporeadds.sporeaddsmod.hive.HiveDownedManager.resolveChoice(player, surrender);
        });
        context.setPacketHandled(true);
    }
}
