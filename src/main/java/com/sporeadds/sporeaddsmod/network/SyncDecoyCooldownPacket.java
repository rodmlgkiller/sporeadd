package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.DecoyCooldownClientState;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public class SyncDecoyCooldownPacket {

    private final int cooldownTicks;

    public SyncDecoyCooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public SyncDecoyCooldownPacket(FriendlyByteBuf buf) {
        this.cooldownTicks = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.cooldownTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
                DecoyCooldownClientState.startCooldown(cooldownTicks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}