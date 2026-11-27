package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.WeakPointClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncWeakPointPacket {

    private final int entityId;
    private final boolean active;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public SyncWeakPointPacket(int entityId, boolean active, Vec3 offset) {
        this.entityId = entityId;
        this.active = active;
        this.offsetX = offset != null ? offset.x : 0;
        this.offsetY = offset != null ? offset.y : 0;
        this.offsetZ = offset != null ? offset.z : 0;
    }

    public SyncWeakPointPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.active = buf.readBoolean();
        this.offsetX = buf.readDouble();
        this.offsetY = buf.readDouble();
        this.offsetZ = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(active);
        buf.writeDouble(offsetX);
        buf.writeDouble(offsetY);
        buf.writeDouble(offsetZ);
    }

    public static void handle(SyncWeakPointPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> onClient(packet));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(SyncWeakPointPacket packet) {
        if (packet.active) {
            WeakPointClientState.set(packet.entityId, new Vec3(packet.offsetX, packet.offsetY, packet.offsetZ));
        } else {
            WeakPointClientState.remove(packet.entityId);
        }
    }
}