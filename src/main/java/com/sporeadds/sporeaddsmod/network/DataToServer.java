package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the ability toggle string after the player flipped a toggle. */
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

    /** The toggle string is exactly nine '0'/'1' characters; anything else is a broken or hostile client. */
    private static boolean isValid(String value) {
        if (value == null || value.length() != 9) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '0' && c != '1') {
                return false;
            }
        }
        return true;
    }

    public static void handle(DataToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (!isValid(msg.data)) {
                // Put the client back in line with the server instead of trusting a malformed string.
                ServerToData.syncTo(player);
                return;
            }
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(cap -> {
                cap.setSwitch(msg.data);
                // Echo with the server -> client packet (this class is only the client -> server direction).
                ServerToData.syncTo(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
