package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncGasSpheresPacket {

    public static class Sphere {
        public final int entityId;
        public final double x;
        public final double y;
        public final double z;
        public final float radius;

        public Sphere(int entityId, double x, double y, double z, float radius) {
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }

    private final List<Sphere> spheres;

    public SyncGasSpheresPacket(List<Sphere> spheres) {
        this.spheres = spheres;
    }

    public static void encode(SyncGasSpheresPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.spheres.size());
        for (Sphere sphere : packet.spheres) {
            buffer.writeInt(sphere.entityId);
            buffer.writeDouble(sphere.x);
            buffer.writeDouble(sphere.y);
            buffer.writeDouble(sphere.z);
            buffer.writeFloat(sphere.radius);
        }
    }

    public static SyncGasSpheresPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Sphere> spheres = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            spheres.add(new Sphere(buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat()));
        }
        return new SyncGasSpheresPacket(spheres);
    }

    public static void handle(SyncGasSpheresPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.sporeadds.sporeaddsmod.client.ClientGasSphereData.update(packet.spheres)
                )
        );
        context.setPacketHandled(true);
    }
}
