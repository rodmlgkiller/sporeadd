package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerData;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.network.EyesDataSyncPacket;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncSporeIdentifierPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME)
public class PlayerTrackingSyncHandler {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        if (!(event.getTarget() instanceof Player target)) return;

        // 1. Sincronizar Ojos (Lo que ya tenías)
        PlayerData data = PlayerDataProvider.get(target);
        NetworkHandle.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> tracker),
                new EyesDataSyncPacket(
                        target.getId(),
                        data.getEyeBaseType(),
                        data.getEyeGlowType(),
                        data.getGlowOffsetX(),
                        data.getGlowOffsetY()
                )
        );

        // 2. Sincronizar Spore Identifier y Subclase (Lo nuevo)
        target.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(sporeData -> {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> tracker),
                    new SyncSporeIdentifierPacket(
                            target.getId(),
                            sporeData.getIdentifier(),
                            sporeData.getSubclass()
                    )
            );
        });
    }
}