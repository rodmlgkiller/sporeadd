package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.CamouflageCooldownClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCamouflageCooldownPacket {

    private final int ticks;

    public SyncCamouflageCooldownPacket(int ticks) {
        this.ticks = ticks;
    }

    public static void encode(SyncCamouflageCooldownPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.ticks);
    }

    public static SyncCamouflageCooldownPacket decode(FriendlyByteBuf buf) {
        return new SyncCamouflageCooldownPacket(buf.readInt());
    }

    public static void handle(SyncCamouflageCooldownPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> CamouflageCooldownClientState.setCooldown(msg.ticks));
        ctx.setPacketHandled(true);
    }
}