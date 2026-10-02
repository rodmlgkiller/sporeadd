package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncFieldResearchCooldownPacket {

    private final int cooldownTicks;

    public SyncFieldResearchCooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public SyncFieldResearchCooldownPacket(FriendlyByteBuf buf) {
        this.cooldownTicks = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(cooldownTicks);
    }

    public static void handle(SyncFieldResearchCooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> onClient(packet.cooldownTicks));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(int cooldownTicks) {
        com.sporeadds.sporeaddsmod.client.FieldResearchCooldownClientState.setCooldown(cooldownTicks);
    }
}