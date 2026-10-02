package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.DelayedDefibrillationCooldownClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDelayedDefibrillationCooldownPacket {

    private final int cooldownTicks;

    public SyncDelayedDefibrillationCooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public SyncDelayedDefibrillationCooldownPacket(FriendlyByteBuf buf) {
        this.cooldownTicks = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(cooldownTicks);
    }

    public static void handle(SyncDelayedDefibrillationCooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                DelayedDefibrillationCooldownClientState.setCooldown(packet.cooldownTicks);
            }
        });
        context.setPacketHandled(true);
    }
}