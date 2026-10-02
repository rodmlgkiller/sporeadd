package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.gui.ClientVervaCountdownData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncVervaCountdownPacket {
    private final UUID transportId;
    private final int seconds;
    private final int priority;
    private final boolean active;

    public SyncVervaCountdownPacket(UUID transportId, int seconds, int priority, boolean active) {
        this.transportId = transportId;
        this.seconds = seconds;
        this.priority = priority;
        this.active = active;
    }

    public static void encode(SyncVervaCountdownPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.transportId);
        buf.writeInt(msg.seconds);
        buf.writeInt(msg.priority);
        buf.writeBoolean(msg.active);
    }

    public static SyncVervaCountdownPacket decode(FriendlyByteBuf buf) {
        UUID transportId = buf.readUUID();
        int seconds = buf.readInt();
        int priority = buf.readInt();
        boolean active = buf.readBoolean();
        return new SyncVervaCountdownPacket(transportId, seconds, priority, active);
    }

    public static void handle(SyncVervaCountdownPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.active) {
                ClientVervaCountdownData.upsert(msg.transportId, msg.seconds, msg.priority);
            } else {
                ClientVervaCountdownData.remove(msg.transportId);
            }
        });
        ctx.setPacketHandled(true);
    }
}