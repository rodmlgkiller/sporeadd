package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
                    player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(cap -> {
                        cap.setSpore(msg.spore);
                    });
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void syncManaToClient(ServerPlayer player) {
        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(cap -> {
            NetworkHandle.INSTANCE.sendTo(
                    new SyncSporePacket(cap.getSpore()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}
