package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import com.sporeadds.sporeaddsmod.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateMoundLocationPacket {

    private final UUID moundId;
    private final String dimension;
    private final int x;
    private final int y;
    private final int z;

    public UpdateMoundLocationPacket(UUID moundId, String dimension, int x, int y, int z) {
        this.moundId = moundId;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(UpdateMoundLocationPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.moundId);
        buf.writeUtf(msg.dimension);
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
        buf.writeInt(msg.z);
    }

    public static UpdateMoundLocationPacket decode(FriendlyByteBuf buf) {
        return new UpdateMoundLocationPacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(UpdateMoundLocationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            sender.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
                if (msg.dimension == null || msg.dimension.isBlank()) return;

                spore.getMoundRegistry().updateLastKnownLocation(
                        msg.moundId,
                        msg.dimension,
                        msg.x,
                        msg.y,
                        msg.z
                );

                CompoundTag nbt = new CompoundTag();
                spore.saveNBTData(nbt);

                NetworkHandle.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sender),
                        new SyncMoundCountPacket(nbt)
                );
            });
        });

        context.setPacketHandled(true);
    }
}