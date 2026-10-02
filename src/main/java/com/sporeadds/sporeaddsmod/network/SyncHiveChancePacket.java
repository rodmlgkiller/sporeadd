package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S -> C. Envía al cliente el valor de servidor de {@code call_of_the_hive_chance}
 * (los config COMMON de Forge no se sincronizan solos). Lo usa la descripción de la clase
 * kommandant en el training book.
 */
public class SyncHiveChancePacket {

    private final double chance;

    public SyncHiveChancePacket(double chance) {
        this.chance = chance;
    }

    public static void encode(SyncHiveChancePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.chance);
    }

    public static SyncHiveChancePacket decode(FriendlyByteBuf buf) {
        return new SyncHiveChancePacket(buf.readDouble());
    }

    public static void handle(SyncHiveChancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        double chance = msg.chance;
        ctx.get().enqueueWork(() ->
                com.sporeadds.sporeaddsmod.client.hive.HiveClientConfig.setChance(chance)
        );
        ctx.get().setPacketHandled(true);
    }
}
