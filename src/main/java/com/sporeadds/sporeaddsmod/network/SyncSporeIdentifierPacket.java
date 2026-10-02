package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSporeIdentifierPacket {

    private final int playerId;
    private final String identifier;
    private final String subclass;

    public SyncSporeIdentifierPacket(int playerId, String identifier) {
        this.playerId = playerId;
        this.identifier = identifier != null ? identifier : "none";
        this.subclass = "none";
    }

    public SyncSporeIdentifierPacket(int playerId, String identifier, String subclass) {
        this.playerId = playerId;
        this.identifier = identifier != null ? identifier : "none";
        this.subclass = subclass != null ? subclass : "none";
    }

    public SyncSporeIdentifierPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.identifier = buf.readUtf(32767);
        this.subclass = buf.readUtf(32767);
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getSubclass() {
        return subclass;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeUtf(identifier);
        buf.writeUtf(subclass);
    }

    public static SyncSporeIdentifierPacket decode(FriendlyByteBuf buf) {
        return new SyncSporeIdentifierPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.network.ClientPacketHandlers.handleSyncSporeIdentifier(this);
        });
        context.setPacketHandled(true);
    }
}