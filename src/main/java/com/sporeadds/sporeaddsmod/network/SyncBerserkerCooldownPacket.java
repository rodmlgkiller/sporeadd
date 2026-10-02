package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.client.BerserkerCooldownClientState;
import net.minecraft.network.FriendlyByteBuf;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

/** S -> C: fija el cooldown de una habilidad de berserker (0 = Counter, 1 = Claws). */
public class SyncBerserkerCooldownPacket {

    private final int which;
    private final int ticks;

    public SyncBerserkerCooldownPacket(int which, int ticks) {
        this.which = which;
        this.ticks = ticks;
    }

    public static void encode(SyncBerserkerCooldownPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.which);
        buf.writeVarInt(msg.ticks);
    }

    public static SyncBerserkerCooldownPacket decode(FriendlyByteBuf buf) {
        return new SyncBerserkerCooldownPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(SyncBerserkerCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BerserkerCooldownClientState.setCooldown(msg.which, msg.ticks));
        ctx.get().setPacketHandled(true);
    }
}
