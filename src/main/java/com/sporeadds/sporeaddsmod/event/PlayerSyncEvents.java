package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncEvolutionCostsPacket;
import com.sporeadds.sporeaddsmod.network.SyncHiveChancePacket;
import com.sporeadds.sporeaddsmod.Powers.Poder1;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerSyncEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncEvolutionCostsPacket(Poder1.getServerEvolutionCostsSnapshot())
            );
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncHiveChancePacket(SporeAddsConfig.CALL_OF_THE_HIVE_CHANCE.get())
            );
        }
    }
}