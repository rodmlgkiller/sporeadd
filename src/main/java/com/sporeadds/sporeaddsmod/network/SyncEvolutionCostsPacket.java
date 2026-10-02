package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.sync.ClientEvolutionCosts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEvolutionCostsPacket {

    private final String[] costs; // index 1..9

    public SyncEvolutionCostsPacket(String[] costs) {
        this.costs = costs;
    }

    public static void encode(SyncEvolutionCostsPacket msg, FriendlyByteBuf buf) {
        for (int i = 1; i <= 9; i++) {
            buf.writeUtf(msg.costs[i] == null ? "" : msg.costs[i], 32767);
        }
    }

    public static SyncEvolutionCostsPacket decode(FriendlyByteBuf buf) {
        String[] costs = new String[10];
        for (int i = 1; i <= 9; i++) {
            costs[i] = buf.readUtf(32767);
        }
        return new SyncEvolutionCostsPacket(costs);
    }

    public static void handle(SyncEvolutionCostsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientEvolutionCosts.setAll(msg.costs);
                })
        );
        ctx.get().setPacketHandled(true);
    }
}