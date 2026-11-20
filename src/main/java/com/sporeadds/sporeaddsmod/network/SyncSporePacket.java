package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import com.sporeadds.sporeaddsmod.util.DistExecutor;
import com.sporeadds.sporeaddsmod.network.NetworkDirection;
import com.sporeadds.sporeaddsmod.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSporePacket {
    private final int spore;
    /** Biomass bar maximum of the player at this moment (kommandant_abilities.biomass_bar). */
    private final int max;

    public SyncSporePacket(int spore, int max) {
        this.spore = spore;
        this.max = max;
    }

    public static void encode(SyncSporePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.spore);
        buf.writeInt(msg.max);
    }

    public static SyncSporePacket decode(FriendlyByteBuf buf) {
        return new SyncSporePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SyncSporePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.sporeadds.sporeaddsmod.util.DistExecutor.unsafeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT, () -> () -> com.sporeadds.sporeaddsmod.client.ClientPacketHooks.syncSpore(msg.spore, msg.max)));
        ctx.get().setPacketHandled(true);
    }

    public static void syncManaToClient(ServerPlayer player) {
        PlayerSporeProvider.PLAYER_CAP.get(player).ifPresent(cap -> {
            // The bar maximum follows the evolution level; recomputed here because this runs for every player every tick.
            int level = PlayerLevelProvider.PLAYER_LVL.get(player)
                    .map(levelCap -> levelCap.getLevel())
                    .orElse(0);
            cap.setMaxSpore(KommandantPowersConfig.biomassBarMax(level));

            NetworkHandle.INSTANCE.sendTo(
                    new SyncSporePacket(cap.getSpore(), cap.getMaxSpore()),
                    player.connection.getConnection(),
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}
