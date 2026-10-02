package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.bile.gluttonousAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class gluttonousShotPacket {

    public gluttonousShotPacket() {
    }

    public static void encode(gluttonousShotPacket msg, FriendlyByteBuf buf) {
    }

    public static gluttonousShotPacket decode(FriendlyByteBuf buf) {
        return new gluttonousShotPacket();
    }

    public static void handle(gluttonousShotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                gluttonousAbilityHandler.tryFiregluttonousBurst(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}