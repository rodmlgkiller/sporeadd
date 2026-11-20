package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.items.BiomassCoreItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the item the player picked on the Biomass Core action wheel. */
public class BiomassCoreMoldPacket {
    private static final int MAX_CHOICE_LENGTH = 64;

    private final String choice;

    public BiomassCoreMoldPacket(String choice) {
        this.choice = choice;
    }

    public static void encode(BiomassCoreMoldPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.choice, MAX_CHOICE_LENGTH);
    }

    public static BiomassCoreMoldPacket decode(FriendlyByteBuf buf) {
        return new BiomassCoreMoldPacket(buf.readUtf(MAX_CHOICE_LENGTH));
    }

    public static void handle(BiomassCoreMoldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BiomassCoreItem.handleMoldChoice(player, msg.choice);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
