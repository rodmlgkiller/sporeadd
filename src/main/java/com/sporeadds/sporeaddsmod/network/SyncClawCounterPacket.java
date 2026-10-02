package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.ClawCounterClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S -> C (solo al jugador): valor del contador de Brutality y si sus garras están activas. */
public class SyncClawCounterPacket {

    private final float value;
    private final boolean active;

    public SyncClawCounterPacket(float value, boolean active) {
        this.value = value;
        this.active = active;
    }

    public static void encode(SyncClawCounterPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.value);
        buf.writeBoolean(msg.active);
    }

    public static SyncClawCounterPacket decode(FriendlyByteBuf buf) {
        return new SyncClawCounterPacket(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(SyncClawCounterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClawCounterClientState.set(msg.value, msg.active));
        ctx.get().setPacketHandled(true);
    }
}
