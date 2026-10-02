package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerToData {
    private final String value;

    public ServerToData(String value) {
        this.value = value;
    }

    public static void encode(ServerToData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.value);
    }

    public static ServerToData decode(FriendlyByteBuf buf) {
        return new ServerToData(buf.readUtf());
    }

    public static void handle(ServerToData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().player.getCapability(PlayerDataProvider.PLAYER_DATA)
                        .ifPresent(cap -> cap.setSwitch(msg.value));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
