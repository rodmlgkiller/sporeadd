package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class DataToServer{
    private final String data;

    public DataToServer(String data) {
        this.data = data;
    }

    public static void encode(DataToServer msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.data);
    }

    public static DataToServer decode(FriendlyByteBuf buf) {
        return new DataToServer(buf.readUtf());
    }

    public static void handle(DataToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(cap -> {
                cap.setSwitch(msg.data);
                NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new DataToServer(cap.getSwitch()));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

