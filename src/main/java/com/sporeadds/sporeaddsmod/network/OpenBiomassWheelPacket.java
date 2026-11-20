package com.sporeadds.sporeaddsmod.network;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: "show the Biomass Core action wheel". Sent when a player right-clicks the Biomass Core while
 * {@code biomass_core.action_wheel_enabled} is on in the server's config.
 */
public class OpenBiomassWheelPacket {

    public OpenBiomassWheelPacket() {
    }

    public static void encode(OpenBiomassWheelPacket msg, net.minecraft.network.FriendlyByteBuf buf) {
    }

    public static OpenBiomassWheelPacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new OpenBiomassWheelPacket();
    }

    public static void handle(OpenBiomassWheelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.sporeadds.sporeaddsmod.client.actionwheel.BiomassCoreWheelClient.open();
        });
        ctx.get().setPacketHandled(true);
    }
}
