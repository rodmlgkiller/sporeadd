package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.CamouflageClientState;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCamouflagePacket {

    private final int entityId;
    private final boolean camouflaged;

    public SyncCamouflagePacket(int entityId, boolean camouflaged) {
        this.entityId = entityId;
        this.camouflaged = camouflaged;
    }

    public static void encode(SyncCamouflagePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.camouflaged);
    }

    public static SyncCamouflagePacket decode(FriendlyByteBuf buf) {
        return new SyncCamouflagePacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(SyncCamouflagePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            CamouflageClientState.setCamouflaged(msg.entityId, msg.camouflaged);
        });
        ctx.setPacketHandled(true);
    }
}