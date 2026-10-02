package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExposeWeaknessCooldownPacket {

    private final int cooldownTicks;

    public SyncExposeWeaknessCooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public SyncExposeWeaknessCooldownPacket(FriendlyByteBuf buf) {
        this.cooldownTicks = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(cooldownTicks);
    }

    public static void handle(SyncExposeWeaknessCooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> onClient(packet.cooldownTicks));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(int cooldownTicks) {
        com.sporeadds.sporeaddsmod.client.ExposeWeaknessCooldownClientState.setCooldown(cooldownTicks);
    }
}