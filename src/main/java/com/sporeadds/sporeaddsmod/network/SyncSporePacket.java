package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import com.sporeadds.sporeaddsmod.level.PlayerLevelProvider;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(cap -> {
                        // The maximum first: setSpore clamps to it.
                        cap.setMaxSpore(msg.max);
                        cap.setSpore(msg.spore);
                    });
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void syncManaToClient(ServerPlayer player) {
        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(cap -> {
            // The bar maximum follows the evolution level; recomputed here because this runs for every player every tick.
            int level = player.getCapability(PlayerLevelProvider.PLAYER_LVL)
                    .map(levelCap -> levelCap.getLevel())
                    .orElse(0);
            cap.setMaxSpore(KommandantPowersConfig.biomassBarMax(level));

            NetworkHandle.INSTANCE.sendTo(
                    new SyncSporePacket(cap.getSpore(), cap.getMaxSpore()),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}
