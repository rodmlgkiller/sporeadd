package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.abilities.DecoyAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class ActivateDecoyPacket {

    private final int playerId;

    public ActivateDecoyPacket(int playerId) {
        this.playerId = playerId;
    }

    public ActivateDecoyPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.playerId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                DecoyAbility.tryActivate(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}