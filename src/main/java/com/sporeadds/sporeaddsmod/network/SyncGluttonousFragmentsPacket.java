package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.ClientGluttonousFragmentsState;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** S -> C (solo al jugador): lista actual de fragmentos de munición del gluttonous (mismo formato que FRAGMENTS_TAG, separado por comas). */
public class SyncGluttonousFragmentsPacket {

    private final String fragmentsCsv;

    public SyncGluttonousFragmentsPacket(String fragmentsCsv) {
        this.fragmentsCsv = fragmentsCsv;
    }

    public static void encode(SyncGluttonousFragmentsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.fragmentsCsv);
    }

    public static SyncGluttonousFragmentsPacket decode(FriendlyByteBuf buf) {
        return new SyncGluttonousFragmentsPacket(buf.readUtf());
    }

    public static void handle(SyncGluttonousFragmentsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientGluttonousFragmentsState.set(msg.fragmentsCsv));
        ctx.get().setPacketHandled(true);
    }
}
