package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: the player's ability toggle string ("Switches" of {@link com.sporeadds.sporeaddsmod.PlayerData.PlayerData}).
 * The server is the source of truth; send it with {@link #syncTo(net.minecraft.server.level.ServerPlayer)} whenever the client
 * may hold a stale copy (login, respawn, dimension change, opening the ability selector).
 */
public class ServerToData {
    private final String value;

    public ServerToData(String value) {
        this.value = value;
    }

    /** Sends the server's current toggle string for {@code player} to that player's client. */
    public static void syncTo(net.minecraft.server.level.ServerPlayer player) {
        if (player == null) {
            return;
        }
        PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data ->
                NetworkHandle.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ServerToData(data.getSwitch())));
    }

    public static void encode(ServerToData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.value);
    }

    public static ServerToData decode(FriendlyByteBuf buf) {
        return new ServerToData(buf.readUtf());
    }

    public static void handle(ServerToData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.serverToData(msg.value)));
        ctx.get().setPacketHandled(true);
    }
}
