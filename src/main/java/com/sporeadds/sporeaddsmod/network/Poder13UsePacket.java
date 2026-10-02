package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.Poder13;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class Poder13UsePacket {
    public Poder13UsePacket() {}

    public static void encode(Poder13UsePacket msg, FriendlyByteBuf buf) {
        // No data
    }

    public static Poder13UsePacket decode(FriendlyByteBuf buf) {
        return new Poder13UsePacket();
    }

    public static void handle(Poder13UsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                // Ejecuta la lógica principal del poder (bomba, efectos, etc)
                // El mensaje "nuke_incoming" ya se encarga de enviarlo activate() si tiene éxito
                Poder13.activate(ctx.get().getSender().serverLevel(), ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}