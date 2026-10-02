package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.BerserkerClawRenderState;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** S -> C (tracking + self): marca a un jugador con las garras activas para el render. */
public class SyncClawsActivePacket {

    private final int entityId;
    private final boolean active;

    public SyncClawsActivePacket(int entityId, boolean active) {
        this.entityId = entityId;
        this.active = active;
    }

    public static void encode(SyncClawsActivePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.active);
    }

    public static SyncClawsActivePacket decode(FriendlyByteBuf buf) {
        return new SyncClawsActivePacket(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncClawsActivePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BerserkerClawRenderState.set(msg.entityId, msg.active));
        ctx.get().setPacketHandled(true);
    }
}
