package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S -> C. Marca (o desmarca) a un jugador como "downed but not out" de la colmena
 * para todos los clientes que lo estén trackeando, de modo que el render lo dibuje
 * tumbado en el suelo.
 */
public class SyncHiveDownedPacket {

    private final int entityId;
    private final boolean downed;

    public SyncHiveDownedPacket(int entityId, boolean downed) {
        this.entityId = entityId;
        this.downed = downed;
    }

    public static void encode(SyncHiveDownedPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.downed);
    }

    public static SyncHiveDownedPacket decode(FriendlyByteBuf buf) {
        return new SyncHiveDownedPacket(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncHiveDownedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                com.sporeadds.sporeaddsmod.client.hive.HiveClientHooks.setDowned(msg.entityId, msg.downed)
        );
        ctx.get().setPacketHandled(true);
    }
}
