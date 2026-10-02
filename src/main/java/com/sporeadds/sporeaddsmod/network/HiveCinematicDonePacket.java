package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C -> S. El cliente ha terminado toda la secuencia cinemática (incluido el bloque
 * de diálogo post-decisión y la transición inversa de pantalla/sonido).
 * El servidor finaliza: en "perish" aplica spore:mycelium_ef y mata al jugador con
 * el daño original; en "arise" simplemente confirma la limpieza de estado.
 */
public class HiveCinematicDonePacket {

    public HiveCinematicDonePacket() {
    }

    public static void encode(HiveCinematicDonePacket msg, FriendlyByteBuf buf) {
    }

    public static HiveCinematicDonePacket decode(FriendlyByteBuf buf) {
        return new HiveCinematicDonePacket();
    }

    public static void handle(HiveCinematicDonePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            com.sporeadds.sporeaddsmod.hive.HiveDownedManager.onCinematicDone(player);
        });
        context.setPacketHandled(true);
    }
}
