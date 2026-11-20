package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataClient;

import java.util.function.Supplier;

public class ArmorHpSyncPacket {
    public final int armorHp;
    /**
     * The player's max Armor HP as the SERVER computes it (it is configurable, and the client's own config file may
     * differ on a dedicated server). -1 = unknown: the HUD then falls back to its own calculation.
     */
    public final int maxArmorHp;

    public ArmorHpSyncPacket(int armorHp) {
        this(armorHp, -1);
    }

    public ArmorHpSyncPacket(int armorHp, int maxArmorHp) {
        this.armorHp = armorHp;
        this.maxArmorHp = maxArmorHp;
    }

    public static void encode(ArmorHpSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.armorHp);
        buf.writeInt(msg.maxArmorHp);
    }

    public static ArmorHpSyncPacket decode(FriendlyByteBuf buf) {
        int armorHp = buf.readInt();
        int maxArmorHp = buf.readInt();
        return new ArmorHpSyncPacket(armorHp, maxArmorHp);
    }

    public static void handle(ArmorHpSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Solo ejecuta en cliente, nunca en server dedicado
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PlayerDataClient.setClientArmorHp(msg.armorHp);
                PlayerDataClient.setClientMaxArmorHp(msg.maxArmorHp);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
