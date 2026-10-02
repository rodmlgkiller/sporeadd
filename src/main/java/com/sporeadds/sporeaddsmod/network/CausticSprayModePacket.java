package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S->C: tells the local player's client to enter Caustic's skill 9 spray mode with a full ammo bar. */
public class CausticSprayModePacket {

    public CausticSprayModePacket() {
    }

    public CausticSprayModePacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static CausticSprayModePacket decode(FriendlyByteBuf buf) {
        return new CausticSprayModePacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.sporeadds.sporeaddsmod.client.AbilityKeyHandler.activateCausticSprayMode()));
        ctx.setPacketHandled(true);
    }
}
