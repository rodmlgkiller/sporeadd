package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.SubclassAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class CausticShotPacket {
    private final int ticksCharged;

    public CausticShotPacket(int ticksCharged) {
        this.ticksCharged = ticksCharged;
    }

    public CausticShotPacket(FriendlyByteBuf buf) {
        this.ticksCharged = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(ticksCharged);
    }

    // ESTE ES EL MÉTODO QUE FALTABA
    public static CausticShotPacket decode(FriendlyByteBuf buf) {
        return new CausticShotPacket(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                SubclassAbility.fireCausticShot(player, ticksCharged);
            }
        });
        ctx.setPacketHandled(true);
    }
}