package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.SelfDefibrillateCooldownClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSelfDefibrillateCooldownPacket {

    private final int cooldownTicks;

    public SyncSelfDefibrillateCooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public SyncSelfDefibrillateCooldownPacket(FriendlyByteBuf buf) {
        this.cooldownTicks = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.cooldownTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
                SelfDefibrillateCooldownClientState.startCooldown(this.cooldownTicks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}