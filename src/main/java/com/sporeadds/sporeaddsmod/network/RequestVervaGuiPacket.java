package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class RequestVervaGuiPacket {

    public RequestVervaGuiPacket() {}

    public static void encode(RequestVervaGuiPacket msg, FriendlyByteBuf buf) {}

    public static RequestVervaGuiPacket decode(FriendlyByteBuf buf) {
        return new RequestVervaGuiPacket();
    }

    public static void handle(RequestVervaGuiPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // El servidor lee su propia configuración
                List<String> verwa = (List<String>) SporeAddsConfig.VERWA_SUMMONING_MENU.get();
                List<String> organoid = (List<String>) SporeAddsConfig.ORGANOID_SUMMONING_MENU.get();
                List<String> bomb = (List<String>) SporeAddsConfig.BOMB_SUMMONING_MENU.get();

                // Y se la envía al cliente para que abra la interfaz
                NetworkHandle.INSTANCE.sendTo(new SyncVervaGuiPacket(verwa, organoid, bomb),
                        player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        context.setPacketHandled(true);
    }
}