package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.Poder6;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Poder5UsePacket {
    private final int entityId;

    public Poder5UsePacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(Poder5UsePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static Poder5UsePacket decode(FriendlyByteBuf buf) {
        return new Poder5UsePacket(buf.readInt());
    }

    public static void handle(Poder5UsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            Entity target = sender.level().getEntity(msg.entityId);
            if (target == null) return;

            Poder6.tryForceEvolve(sender, target);
        });

        context.setPacketHandled(true);
    }
}