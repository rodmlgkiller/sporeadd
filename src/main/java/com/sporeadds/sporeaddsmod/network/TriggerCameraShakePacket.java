package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerCameraShakePacket {

    private final int durationTicks;
    private final float intensity;

    public TriggerCameraShakePacket(int durationTicks, float intensity) {
        this.durationTicks = durationTicks;
        this.intensity = intensity;
    }

    public static void encode(TriggerCameraShakePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.durationTicks);
        buffer.writeFloat(packet.intensity);
    }

    public static TriggerCameraShakePacket decode(FriendlyByteBuf buffer) {
        return new TriggerCameraShakePacket(buffer.readInt(), buffer.readFloat());
    }

    public static void handle(TriggerCameraShakePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.sporeadds.sporeaddsmod.client.ClientCameraShakeData.start(packet.durationTicks, packet.intensity)
                )
        );
        context.setPacketHandled(true);
    }
}
