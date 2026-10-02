package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.SubclassAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C->S: fired every 2 client ticks while the fire key is held during Caustic's skill 9 spray mode. */
public class FireCausticSprayShotPacket {

    public FireCausticSprayShotPacket() {
    }

    public FireCausticSprayShotPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static FireCausticSprayShotPacket decode(FriendlyByteBuf buf) {
        return new FireCausticSprayShotPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                SubclassAbility.fireCausticSpraySegment(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
