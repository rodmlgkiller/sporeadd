package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSporePacket {
    private final int spore;

    public SyncSporePacket(int spore) {
        this.spore = spore;
    }

    public static void encode(SyncSporePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.spore);
    }

    public static SyncSporePacket decode(FriendlyByteBuf buf) {
        return new SyncSporePacket(buf.readInt());
    }

    public static void handle(SyncSporePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(cap -> {
                        cap.setSpore(msg.spore);
                    });
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void syncManaToClient(ServerPlayer player) {
        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(cap -> {
            NetworkHandle.INSTANCE.sendTo(
                    new SyncSporePacket(cap.getSpore()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}
