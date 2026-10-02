package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncgluttonousCrosshairRenderPacket {

    private final int playerId;

    public SyncgluttonousCrosshairRenderPacket(int playerId) {
        this.playerId = playerId;
    }

    public SyncgluttonousCrosshairRenderPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
    }

    public int getPlayerId() {
        return playerId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
    }

    public static SyncgluttonousCrosshairRenderPacket decode(FriendlyByteBuf buf) {
        return new SyncgluttonousCrosshairRenderPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.network.ClientPacketHandlers.handleSyncgluttonousCrosshairRender(this);
        });
        context.setPacketHandled(true);
    }
}