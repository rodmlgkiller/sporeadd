package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Server -> client: the player's ability toggle string ("Switches" of {@link com.sporeadds.sporeaddsmod.PlayerData.PlayerData}).
 * The server is the source of truth; send it with {@link #syncTo(ServerPlayer)} whenever the client may hold a stale copy
 * (login, respawn, dimension change, opening the ability selector).
 */
public class ServerToData {
    private final String value;

    public ServerToData(String value) {
        this.value = value;
    }

    /** Sends the server's current toggle string for {@code player} to that player's client. */
    public static void syncTo(ServerPlayer player) {
        if (player == null) {
            return;
        }
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data ->
                NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ServerToData(data.getSwitch())));
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
                if (Minecraft.getInstance().player == null) {
                    return;
                }
                Minecraft.getInstance().player.getCapability(PlayerDataProvider.PLAYER_DATA)
                        .ifPresent(cap -> cap.setSwitch(msg.value));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
