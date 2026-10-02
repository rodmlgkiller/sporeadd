package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.PowerBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PowerUseHandler {
    private final int id;

    public PowerUseHandler(int id) {
        this.id = id;
    }

    public static void encode(PowerUseHandler msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
    }

    public static PowerUseHandler decode(FriendlyByteBuf buf) {
        return new PowerUseHandler(buf.readInt());
    }

    public static void handle(PowerUseHandler msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PowerBase power = PowerRegistry.getPower(msg.id);
                if (power != null) {
                    power.use(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}