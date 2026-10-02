package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.abilities.FieldResearchAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActivateFieldResearchPacket {

    public ActivateFieldResearchPacket() {
    }

    public ActivateFieldResearchPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(ActivateFieldResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                FieldResearchAbility.tryActivate(player);
            }
        });
        context.setPacketHandled(true);
    }
}