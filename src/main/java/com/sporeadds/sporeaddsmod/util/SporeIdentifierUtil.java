package com.sporeadds.sporeaddsmod.util;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.data.ClassPopulationData;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncSporeIdentifierPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class SporeIdentifierUtil {

    private static final String DEFAULT_SWITCHES = "00000000000000";

    public static boolean setIdentifierAndSync(ServerPlayer player, String newIdentifier) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).map(data -> {
            boolean changed = data.setIdentifier(newIdentifier);
            if (changed) {
                player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(playerData -> {
                    playerData.setSwitch(DEFAULT_SWITCHES);
                });
                ClassPopulationData.get(player.server).setPlayerClass(player.getUUID(), newIdentifier);

                // Convertirse en kommandant limpia todos los efectos (incl. call_of_the_hive).
                if ("kommandant".equalsIgnoreCase(newIdentifier)) {
                    player.removeAllEffects();
                }

                // Cualquier cambio de clase resetea la escala de Pehkui (equivale al poder
                // sporeadd:originremovescale). Luego forceReapplyStats vuelve a poner la que toque.
                com.sporeadds.sporeaddsmod.Powers.Levelstats.resetPehkuiScales(player);

                // Limpia el estado de las habilidades de berserker al dejar esa clase.
                com.sporeadds.sporeaddsmod.Powers.berserker.CounterAbility.forget(player.getUUID());
                com.sporeadds.sporeaddsmod.Powers.berserker.ClawsAbility.forget(player.getUUID());
                if (!"berserker".equalsIgnoreCase(newIdentifier)) {
                    com.sporeadds.sporeaddsmod.Powers.berserker.CompoundEffects.removeBuffs(player);
                    com.sporeadds.sporeaddsmod.Powers.berserker.CompoundEffects.dropAndClearCompounds(player);
                }

                sync(player);

                // Sincroniza el origin correspondiente (o humano para "none") en cualquier vía.
                OriginSyncUtil.applyForIdentifier(player, newIdentifier);
            }
            return changed;
        }).orElse(false);
    }

    public static void setSubclassAndSync(ServerPlayer player, String newSubclass) {
        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            data.setSubclass(newSubclass);
            sync(player);
        });
    }

    public static void sync(ServerPlayer player) {
        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            NetworkHandle.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncSporeIdentifierPacket(
                            player.getId(),
                            data.getIdentifier(),
                            data.getSubclass()
                    )
            );
        });
    }
}