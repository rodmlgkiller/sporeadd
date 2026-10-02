package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** S -> C (broadcast): current positions of active Abyssal Power13 storms, used to hide vanilla rain nearby. */
public class SyncAbyssalStormPacket {

    public static class Storm {
        public final double x;
        public final double y;
        public final double z;

        public Storm(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final List<Storm> storms;

    public SyncAbyssalStormPacket(List<Storm> storms) {
        this.storms = storms;
    }

    public static void encode(SyncAbyssalStormPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.storms.size());
        for (Storm storm : packet.storms) {
            buffer.writeDouble(storm.x);
            buffer.writeDouble(storm.y);
            buffer.writeDouble(storm.z);
        }
    }

    public static SyncAbyssalStormPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Storm> storms = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            storms.add(new Storm(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new SyncAbyssalStormPacket(storms);
    }

    public static void handle(SyncAbyssalStormPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.sporeadds.sporeaddsmod.client.ClientAbyssalStormState.update(packet.storms)
                )
        );
        context.setPacketHandled(true);
    }
}
