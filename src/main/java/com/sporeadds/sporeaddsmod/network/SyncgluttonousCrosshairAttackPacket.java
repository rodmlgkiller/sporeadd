package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncgluttonousCrosshairAttackPacket {

    private final int playerId;

    public SyncgluttonousCrosshairAttackPacket(int playerId) {
        this.playerId = playerId;
    }

    public SyncgluttonousCrosshairAttackPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
    }

    public int getPlayerId() {
        return playerId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
    }

    public static SyncgluttonousCrosshairAttackPacket decode(FriendlyByteBuf buf) {
        return new SyncgluttonousCrosshairAttackPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.network.ClientPacketHandlers.handleSyncgluttonousCrosshairAttack(this);
        });
        context.setPacketHandled(true);
    }
}