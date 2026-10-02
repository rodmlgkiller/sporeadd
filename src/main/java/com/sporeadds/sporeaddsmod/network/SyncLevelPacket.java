package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLevelPacket {
    private final int level;
    private final int knowledgeLevel;

    public SyncLevelPacket(int level, int knowledgeLevel) {
        this.level = level;
        this.knowledgeLevel = knowledgeLevel;
    }

    public static void encode(SyncLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.knowledgeLevel);
    }

    public static SyncLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncLevelPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SyncLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> {
                        cap.setLevel(msg.level);
                        cap.setKnowledgeLevel(msg.knowledgeLevel);
                    });
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void syncLevelToClient(ServerPlayer player) {
        PlayerLevelProvider.PLAYER_LVL.get(player).ifPresent(cap -> {
            NetworkHandle.INSTANCE.sendTo(
                    new SyncLevelPacket(cap.getLevel(), cap.getKnowledgeLevel()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}