package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.renderer.KommandantESPClient;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncKommandantEspPacket {

    private final int entityId;
    private final boolean hasMarker;
    private final int markerAmplifier;
    private final boolean hasUneasy;
    private final boolean hasSeasoned;

    public SyncKommandantEspPacket(int entityId, boolean hasMarker, int markerAmplifier, boolean hasUneasy, boolean hasSeasoned) {
        this.entityId = entityId;
        this.hasMarker = hasMarker;
        this.markerAmplifier = markerAmplifier;
        this.hasUneasy = hasUneasy;
        this.hasSeasoned = hasSeasoned;
    }

    public static void encode(SyncKommandantEspPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.hasMarker);
        buf.writeInt(msg.markerAmplifier);
        buf.writeBoolean(msg.hasUneasy);
        buf.writeBoolean(msg.hasSeasoned);
    }

    public static SyncKommandantEspPacket decode(FriendlyByteBuf buf) {
        return new SyncKommandantEspPacket(
                buf.readInt(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(SyncKommandantEspPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        KommandantESPClient.updatePlayerEffects(
                                msg.entityId,
                                msg.hasMarker,
                                msg.markerAmplifier,
                                msg.hasUneasy,
                                msg.hasSeasoned
                        )
                )
        );
        ctx.get().setPacketHandled(true);
    }
}