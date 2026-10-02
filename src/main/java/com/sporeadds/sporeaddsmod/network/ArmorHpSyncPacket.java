package com.sporeadds.sporeaddsmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataClient;

import java.util.function.Supplier;

public class ArmorHpSyncPacket {
    public final int armorHp;

    public ArmorHpSyncPacket(int armorHp) {
        this.armorHp = armorHp;
    }

    public static void encode(ArmorHpSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.armorHp);
    }

    public static ArmorHpSyncPacket decode(FriendlyByteBuf buf) {
        return new ArmorHpSyncPacket(buf.readInt());
    }

    public static void handle(ArmorHpSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Solo ejecuta en cliente, nunca en server dedicado
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PlayerDataClient.setClientArmorHp(msg.armorHp);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
