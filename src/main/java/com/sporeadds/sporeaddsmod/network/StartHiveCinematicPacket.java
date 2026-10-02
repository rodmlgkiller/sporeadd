package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S -> C. Ordena al cliente del jugador abrir la secuencia cinemática de la colmena
 * (oscurecido de pantalla + reducción de sonido + diálogo).
 *
 * {@code forceSurrender}: si true, la pantalla solo ofrece el "give up" rojo (rendición).
 */
public class StartHiveCinematicPacket {

    private final boolean forceSurrender;

    public StartHiveCinematicPacket(boolean forceSurrender) {
        this.forceSurrender = forceSurrender;
    }

    public static void encode(StartHiveCinematicPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.forceSurrender);
    }

    public static StartHiveCinematicPacket decode(FriendlyByteBuf buf) {
        return new StartHiveCinematicPacket(buf.readBoolean());
    }

    public static void handle(StartHiveCinematicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        boolean forceSurrender = msg.forceSurrender;
        ctx.get().enqueueWork(() ->
                com.sporeadds.sporeaddsmod.client.hive.HiveClientHooks.openCinematic(forceSurrender)
        );
        ctx.get().setPacketHandled(true);
    }
}
