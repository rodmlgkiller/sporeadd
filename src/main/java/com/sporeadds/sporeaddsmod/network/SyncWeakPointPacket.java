package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.WeakPointClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tells the clients where the weak points of an entity are. Besides the offsets it carries the tier (which sprite to
 * draw: only the server knows the Data behind it) and the weak point size, so the marker drawn on every client
 * matches the server's config.
 */
public class SyncWeakPointPacket {

    /** Hit radius (blocks) for which the marker is drawn at its original size. */
    public static final float DEFAULT_RADIUS = 0.3F;

    private static final int MAX_POINTS = 256;

    private final int entityId;
    private final boolean active;
    private final List<Vec3> offsets;
    private final int tier;
    private final float radius;

    /** One weak point (or none when {@code offset} is null, used to remove it) with the default tier and size. */
    public SyncWeakPointPacket(int entityId, boolean active, Vec3 offset) {
        this(entityId, active, offset != null ? List.of(offset) : List.<Vec3>of(), 0, DEFAULT_RADIUS);
    }

    public SyncWeakPointPacket(int entityId, boolean active, List<Vec3> offsets, int tier, float radius) {
        this.entityId = entityId;
        this.active = active;
        this.offsets = offsets;
        this.tier = tier;
        this.radius = radius;
    }

    public SyncWeakPointPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.active = buf.readBoolean();

        int count = buf.readVarInt();
        if (count < 0 || count > MAX_POINTS) {
            throw new IllegalArgumentException("Bad weak point count: " + count);
        }
        List<Vec3> read = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            read.add(new Vec3(x, y, z));
        }
        this.offsets = read;

        this.tier = buf.readVarInt();
        this.radius = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(active);
        buf.writeVarInt(offsets.size());
        for (Vec3 offset : offsets) {
            buf.writeDouble(offset.x);
            buf.writeDouble(offset.y);
            buf.writeDouble(offset.z);
        }
        buf.writeVarInt(tier);
        buf.writeFloat(radius);
    }

    public static void handle(SyncWeakPointPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> onClient(packet));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(SyncWeakPointPacket packet) {
        if (packet.active) {
            WeakPointClientState.set(packet.entityId, packet.offsets, packet.tier, packet.radius);
        } else {
            WeakPointClientState.remove(packet.entityId);
        }
    }
}