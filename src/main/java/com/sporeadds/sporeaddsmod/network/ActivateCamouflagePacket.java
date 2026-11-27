package com.sporeadds.sporeaddsmod.network;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ActivateCamouflagePacket {

    public static final int CAMOUFLAGE_COOLDOWN_TICKS = 400; // 20 segundos

    private final int entityId;

    public ActivateCamouflagePacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(ActivateCamouflagePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static ActivateCamouflagePacket decode(FriendlyByteBuf buf) {
        return new ActivateCamouflagePacket(buf.readInt());
    }

    public static void handle(ActivateCamouflagePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
                if (data.isCamouflaged()) {
                    CamouflageLogic.deactivate(player, data);
                } else {
                    if (data.isCamouflageOnCooldown()) {
                        return;
                    }
                    CamouflageLogic.activate(player, data);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}