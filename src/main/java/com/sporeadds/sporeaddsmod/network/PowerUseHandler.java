package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.Powers.PowerBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

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
                // The unlock levels (kommandant_abilities.unlock_levels) are enforced here for EVERY ability, so a client whose
                // config differs from the server's cannot use an ability the server has not unlocked yet. This packet is the
                // player's own request: admin tools that call a power directly (/sporeaddstest) are not affected.
                if (msg.id >= 1 && msg.id <= 13
                        && !com.sporeadds.sporeaddsmod.Powers.PowerUnlocks.checkOrWarn(player, msg.id)) {
                    return;
                }
                PowerBase power = PowerRegistry.getPower(msg.id);
                if (power != null) {
                    power.use(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}